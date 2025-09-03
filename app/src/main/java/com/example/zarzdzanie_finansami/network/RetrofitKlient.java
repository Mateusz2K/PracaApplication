package com.example.zarzdzanie_finansami.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class RetrofitKlient {
    //Użyj 10.0.2.2:8443 zamiast localhost, aby połączyć się z serwerem
    // na komputerze z emulatora Androida. Użyj portu 8443 dla HTTPS.
    private static final String BASE_URL = "https://10.0.2.2:8442/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            OkHttpClient client = ZnanyOkHttpClient.createTrustedClient(context);

            // Konfiguracja Gson
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .setDateFormat("dd.MM.yyyy")//nie ma godziny, bo jest dopasowany do DTO
            .create();
            GsonConverterFactory gsonConverter = GsonConverterFactory.create(gson);

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(gsonConverter)
                    .build();
        }
        return retrofit;
    }
}
