package com.example.zarzdzanie_finansami.network.api;

import com.example.zarzdzanie_finansami.dto.transakcja.TransakcjaOdpowiedz;
import com.example.zarzdzanie_finansami.dto.transakcja.TransakcjaPobieranieDTO;
import com.example.zarzdzanie_finansami.dto.transakcja.TransakcjaWysylanie;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiTransakcje {
    @POST("api/transakcje/pobierz") // Upewnij się, że ścieżka jest poprawna
    Call<List<TransakcjaOdpowiedz>> pobierzTransakcjeDynamicznie(
            @Header("Authorization") String authToken,
            @Body TransakcjaPobieranieDTO kryteria
    );
    // Pobieranie pojedynczej transakcji (jeśli potrzebne)
    @GET("api/transakcje/{transakcjaId}")
    Call<TransakcjaOdpowiedz> getTransakcjaById(
            @Header("Authorization") String authToken,
            @Path("transakcjaId") int transakcjaId
    );
    @POST("api/transakcje")
    Call<TransakcjaOdpowiedz> dodajTransakcje(
            @Header("Authorization") String authToken,
            @Body TransakcjaWysylanie transakcjaWysylanie // Potrzebujesz DTO TransakcjaRequest
    );
    // Modyfikacja transakcji
    @PUT("api/transakcje/{transakcjaId}")
    Call<TransakcjaOdpowiedz> modyfikujTransakcje(
            @Header("Authorization") String authToken,
            @Path("transakcjaId") int transakcjaId,
            @Body TransakcjaWysylanie transakcjaWysylanie // Potrzebujesz DTO TransakcjaRequest
    );
    // Usuwanie transakcji
    @DELETE("api/transakcje/{transakcjaId}")
    Call<Void> deleteTransakcja(
            @Header("Authorization") String authToken,
            @Path("transakcjaId") int transakcjaId
    );
}
