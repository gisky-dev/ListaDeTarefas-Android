package br.com.curso.listadetarefas.android.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import br.com.curso.listadetarefas.android.R;
import br.com.curso.listadetarefas.android.model.Tarefa;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder> {

    public interface OnTarefaInteractionListener {
        void onToggleStatus(Tarefa tarefa, boolean novoStatus);
        void onEdit(Tarefa tarefa);
        void onDelete(Tarefa tarefa);
    }

    private final List<Tarefa> tarefas = new ArrayList<>();
    private final OnTarefaInteractionListener listener;

    public TarefaAdapter(OnTarefaInteractionListener listener) {
        this.listener = listener;
    }

    public void setTarefas(List<Tarefa> novasTarefas) {
        this.tarefas.clear();
        if (novasTarefas != null) {
            this.tarefas.addAll(novasTarefas);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TarefaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarefa, parent, false);
        return new TarefaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarefaViewHolder holder, int position) {
        holder.bind(tarefas.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return tarefas.size();
    }

    static class TarefaViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardItemTarefa;
        private final CheckBox cbConcluida;
        private final TextView tvDescricao;
        private final ImageButton btnEditar;
        private final ImageButton btnExcluir;

        public TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItemTarefa = itemView.findViewById(R.id.cardItemTarefa);
            cbConcluida = itemView.findViewById(R.id.cbConcluida);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }

        public void bind(final Tarefa tarefa, final OnTarefaInteractionListener listener) {
            Context context = itemView.getContext();
            tvDescricao.setText(tarefa.getDescricao());

            // Estilização visual (tachado / cor / opacidade)
            if (tarefa.isConcluida()) {
                tvDescricao.setPaintFlags(tvDescricao.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvDescricao.setTextColor(ContextCompat.getColor(context, R.color.text_completed));
                cardItemTarefa.setAlpha(0.85f);
            } else {
                tvDescricao.setPaintFlags(tvDescricao.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvDescricao.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                cardItemTarefa.setAlpha(1.0f);
            }

            // Previne disparos fantasmas ao reciclar
            cbConcluida.setOnCheckedChangeListener(null);
            cbConcluida.setChecked(tarefa.isConcluida());

            cbConcluida.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed() && listener != null) {
                    listener.onToggleStatus(tarefa, isChecked);
                }
            });

            btnEditar.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(tarefa);
            });

            btnExcluir.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(tarefa);
            });
        }
    }
}
