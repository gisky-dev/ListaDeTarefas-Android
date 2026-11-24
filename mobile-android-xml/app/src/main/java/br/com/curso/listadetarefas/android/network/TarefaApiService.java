package br.com.curso.listadetarefas.android.network;

import br.com.curso.listadetarefas.android.model.Tarefa;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface TarefaApiService {

    @GET("api/tarefas")
    Call<List<Tarefa>> listarTodas();

    @GET("api/tarefas/{id}")
    Call<Tarefa> buscarPorId(@Path("id") Long id);

    @POST("api/tarefas")
    Call<Tarefa> criar(@Body Tarefa tarefa);

    @PUT("api/tarefas/{id}")
    Call<Tarefa> atualizar(@Path("id") Long id, @Body Tarefa tarefa);

    @DELETE("api/tarefas/{id}")
    Call<Void> excluir(@Path("id") Long id);
}
