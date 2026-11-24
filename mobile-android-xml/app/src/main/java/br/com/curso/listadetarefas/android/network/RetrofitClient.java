package br.com.curso.listadetarefas.android.network;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    public static final String DEFAULT_BASE_URL = "http://10.0.2.2:8080/";
    private static String currentBaseUrl = DEFAULT_BASE_URL;
    private static TarefaApiService apiService = null;

    private RetrofitClient() {}

    public static synchronized TarefaApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(currentBaseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(TarefaApiService.class);
        }
        return apiService;
    }

    public static synchronized void setBaseUrl(String newBaseUrl) {
        if (!newBaseUrl.endsWith("/")) {
            newBaseUrl += "/";
        }
        currentBaseUrl = newBaseUrl;
        apiService = null; // força recriação na próxima chamada
    }
}
