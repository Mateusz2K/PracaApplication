package com.example.zarzdzanie_finansami.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class RetrofitKlient {
    private static final String BASE_URL = "http://10.0.2.2:8081/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Opcjonalny interceptor do logowania zapytań i odpowiedzi
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor) // Dodaj interceptor
                    .build();

            // Konfiguracja Gson (jeśli potrzebujesz specjalnego formatowania daty itp.)
            Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // Przykładowy format daty
            .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // Ustaw niestandardowego klienta OkHttp
                    .addConverterFactory(GsonConverterFactory.create()) // Użyj domyślnego Gson
                    .build();
        }
        return retrofit;
    }
}
