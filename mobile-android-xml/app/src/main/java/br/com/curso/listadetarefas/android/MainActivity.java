package br.com.curso.listadetarefas.android;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import br.com.curso.listadetarefas.android.adapter.TarefaAdapter;
import br.com.curso.listadetarefas.android.model.Tarefa;
import br.com.curso.listadetarefas.android.network.RetrofitClient;
import br.com.curso.listadetarefas.android.network.TarefaApiService;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements TarefaAdapter.OnTarefaInteractionListener {

    private EditText etNovaTarefa;
    private MaterialButton btnAdicionar;
    private ImageButton btnRecarregar;
    private ProgressBar progressBar;
    private RecyclerView rvTarefas;
    private TextView tvEmptyState;

    private TarefaAdapter adapter;
    private TarefaApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient.getApiService();

        inicializarComponentes();
        configurarRecyclerView();
        configurarEventos();

        carregarTarefas();
    }

    private void inicializarComponentes() {
        etNovaTarefa = findViewById(R.id.etNovaTarefa);
        btnAdicionar = findViewById(R.id.btnAdicionar);
        btnRecarregar = findViewById(R.id.btnRecarregar);
        progressBar = findViewById(R.id.progressBar);
        rvTarefas = findViewById(R.id.rvTarefas);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void configurarRecyclerView() {
        adapter = new TarefaAdapter(this);
        rvTarefas.setLayoutManager(new LinearLayoutManager(this));
        rvTarefas.setAdapter(adapter);
    }

    private void configurarEventos() {
        btnAdicionar.setOnClickListener(v -> adicionarTarefa());
        btnRecarregar.setOnClickListener(v -> carregarTarefas());
    }

    private void setCarregando(boolean carregando) {
        progressBar.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnAdicionar.setEnabled(!carregando);
        btnRecarregar.setEnabled(!carregando);
    }

    private void atualizarVisibilidadeLista(int totalTarefas) {
        if (totalTarefas == 0) {
            rvTarefas.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvTarefas.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void carregarTarefas() {
        setCarregando(true);
        apiService.listarTodas().enqueue(new Callback<List<Tarefa>>() {
            @Override
            public void onResponse(@NonNull Call<List<Tarefa>> call, @NonNull Response<List<Tarefa>> response) {
                setCarregando(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Tarefa> lista = response.body();
                    adapter.setTarefas(lista);
                    atualizarVisibilidadeLista(lista.size());
                } else {
                    Toast.makeText(MainActivity.this, "Erro HTTP: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Tarefa>> call, @NonNull Throwable t) {
                setCarregando(false);
                Toast.makeText(MainActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void adicionarTarefa() {
        String texto = etNovaTarefa.getText() != null ? etNovaTarefa.getText().toString().trim() : "";
        if (texto.isEmpty()) {
            Toast.makeText(this, R.string.erro_campo_vazio, Toast.LENGTH_SHORT).show();
            return;
        }

        Tarefa nova = new Tarefa(texto, false);
        setCarregando(true);

        apiService.criar(nova).enqueue(new Callback<Tarefa>() {
            @Override
            public void onResponse(@NonNull Call<Tarefa> call, @NonNull Response<Tarefa> response) {
                setCarregando(false);
                if (response.isSuccessful() && response.body() != null) {
                    etNovaTarefa.setText("");
                    Toast.makeText(MainActivity.this, R.string.sucesso_adicionada, Toast.LENGTH_SHORT).show();
                    carregarTarefas();
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao salvar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Tarefa> call, @NonNull Throwable t) {
                setCarregando(false);
                Toast.makeText(MainActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onToggleStatus(Tarefa tarefa, boolean novoStatus) {
        tarefa.setConcluida(novoStatus);
        setCarregando(true);

        apiService.atualizar(tarefa.getId(), tarefa).enqueue(new Callback<Tarefa>() {
            @Override
            public void onResponse(@NonNull Call<Tarefa> call, @NonNull Response<Tarefa> response) {
                setCarregando(false);
                carregarTarefas();
            }

            @Override
            public void onFailure(@NonNull Call<Tarefa> call, @NonNull Throwable t) {
                setCarregando(false);
                Toast.makeText(MainActivity.this, "Falha ao atualizar status: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                carregarTarefas();
            }
        });
    }

    @Override
    public void onEdit(Tarefa tarefa) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_tarefa, null);
        EditText etEditar = dialogView.findViewById(R.id.etEditarDescricao);
        etEditar.setText(tarefa.getDescricao());
        etEditar.setSelection(etEditar.getText().length());

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_btn_salvar, (dialog, which) -> {
                    String novoTexto = etEditar.getText().toString().trim();
                    if (novoTexto.isEmpty()) {
                        Toast.makeText(this, R.string.erro_campo_vazio, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    tarefa.setDescricao(novoTexto);
                    salvarEdicao(tarefa);
                })
                .setNegativeButton(R.string.dialog_btn_cancelar, null)
                .show();
    }

    private void salvarEdicao(Tarefa tarefa) {
        setCarregando(true);
        apiService.atualizar(tarefa.getId(), tarefa).enqueue(new Callback<Tarefa>() {
            @Override
            public void onResponse(@NonNull Call<Tarefa> call, @NonNull Response<Tarefa> response) {
                setCarregando(false);
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, R.string.sucesso_atualizada, Toast.LENGTH_SHORT).show();
                    carregarTarefas();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Tarefa> call, @NonNull Throwable t) {
                setCarregando(false);
                Toast.makeText(MainActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDelete(Tarefa tarefa) {
        String msg = getString(R.string.dialog_excluir_msg, tarefa.getDescricao());

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_excluir_titulo)
                .setMessage(msg)
                .setPositiveButton(R.string.dialog_btn_excluir, (dialog, which) -> executarExclusao(tarefa.getId()))
                .setNegativeButton(R.string.dialog_btn_cancelar, null)
                .show();
    }

    private void executarExclusao(Long id) {
        setCarregando(true);
        apiService.excluir(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                setCarregando(false);
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, R.string.sucesso_excluida, Toast.LENGTH_SHORT).show();
                    carregarTarefas();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                setCarregando(false);
                Toast.makeText(MainActivity.this, "Falha ao excluir: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
