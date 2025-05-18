package com.example.zarzdzanie_finansami.network;


import com.example.zarzdzanie_finansami.dto.KontoRequest;
import com.example.zarzdzanie_finansami.dto.KontoResponse;
import com.example.zarzdzanie_finansami.dto.LogowanieRequest;
import com.example.zarzdzanie_finansami.dto.LogowanieResponse;
import com.example.zarzdzanie_finansami.dto.TransakcjaRequest;
import com.example.zarzdzanie_finansami.dto.TransakcjaResponse;

import java.util.List;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.Call;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiSerwis {
    @POST("api/auth/login")
    Call<LogowanieResponse> loginUser(@Body LogowanieRequest loginRequest);

    @GET("api/konta")
    Call<List<KontoResponse>> getMojeKonta(@Header("Authorization") String authToken);

    // Tutaj możesz dodać inne endpointy, np.
    @GET("api/konta/{id}")
    Call<KontoResponse> getKontoById(@Header("Authorization") String authToken, @Path("id") int kontoId);

    // Pobieranie transakcji dla konkretnego konta
    @GET("api/transakcje/konto/{kontoId}")
    Call<List<TransakcjaResponse>> getTransakcjeDlaKonta(
            @Header("Authorization") String authToken,
            @Path("kontoId") int kontoId
    );
    @POST("api/konta")
    Call<KontoResponse> addKonto(
            @Header("Authorization") String authToken,
            @Body KontoRequest kontoRequest
    );

    @PUT("api/konta/{kontoId}")
    Call<KontoResponse> updateKonto(
            @Header("Authorization") String authToken,
            @Path("kontoId") int kontoId,
            @Body KontoRequest kontoRequest
    );

    @DELETE("api/konta/{kontoId}")
    Call<Void> deleteKonto(
            @Header("Authorization") String authToken,
            @Path("kontoId") int kontoId
    );

    // Pobieranie pojedynczej transakcji (jeśli potrzebne)
    @GET("api/transakcje/{transakcjaId}")
    Call<TransakcjaResponse> getTransakcjaById(
            @Header("Authorization") String authToken,
            @Path("transakcjaId") int transakcjaId
    );

    // Dodawanie nowej transakcji
    @POST("api/transakcje")
    Call<TransakcjaResponse> dodajTransakcje(
            @Header("Authorization") String authToken,
            @Body TransakcjaRequest transakcjaRequest // Potrzebujesz DTO TransakcjaRequest
    );

    // Modyfikacja transakcji
    @PUT("api/transakcje/{transakcjaId}")
    Call<TransakcjaResponse> modyfikujTransakcje(
            @Header("Authorization") String authToken,
            @Path("transakcjaId") int transakcjaId,
            @Body TransakcjaRequest transakcjaRequest // Potrzebujesz DTO TransakcjaRequest
    );

    // Usuwanie transakcji
    @DELETE("api/transakcje/{transakcjaId}")
    Call<Void> deleteTransakcja(
            @Header("Authorization") String authToken,
            @Path("transakcjaId") int transakcjaId
    );

}
