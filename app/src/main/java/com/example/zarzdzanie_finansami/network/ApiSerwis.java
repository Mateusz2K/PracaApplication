package com.example.zarzdzanie_finansami.network;


import com.example.zarzdzanie_finansami.dto.KategoriaOdpowiedz;
import com.example.zarzdzanie_finansami.dto.KontoWysylanie;
import com.example.zarzdzanie_finansami.dto.KontoOdpowiedz;
import com.example.zarzdzanie_finansami.dto.LogowanieWysylanie;
import com.example.zarzdzanie_finansami.dto.LogowanieOdpowiedz;
import com.example.zarzdzanie_finansami.dto.TransakcjaWysylanie;
import com.example.zarzdzanie_finansami.dto.TransakcjaOdpowiedz;

import java.util.List;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.Call;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiSerwis {
    @POST("api/auth/login")
    Call<LogowanieOdpowiedz> loginUser(@Body LogowanieWysylanie loginRequest);

    @GET("api/konta")
    Call<List<KontoOdpowiedz>> getMojeKonta(@Header("Authorization") String authToken);

    // Tutaj możesz dodać inne endpointy, np.
    @GET("api/konta/{id}")
    Call<KontoOdpowiedz> getKontoById(@Header("Authorization") String authToken, @Path("id") int kontoId);

    // Pobieranie transakcji dla konkretnego konta
    @GET("api/konta/{kontoId}/transakcje")
    Call<List<TransakcjaOdpowiedz>> getTransakcjeDlaKonta(
            @Header("Authorization") String authToken,
            @Path("kontoId") int kontoId
    );
    @POST("api/konta")
    Call<KontoOdpowiedz> addKonto(
            @Header("Authorization") String authToken,
            @Body KontoWysylanie kontoWysylanie
    );

    @PUT("api/konta/{kontoId}")
    Call<KontoOdpowiedz> updateKonto(
            @Header("Authorization") String authToken,
            @Path("kontoId") int kontoId,
            @Body KontoWysylanie kontoWysylanie
    );

    @DELETE("api/konta/{kontoId}")
    Call<Void> deleteKonto(
            @Header("Authorization") String authToken,
            @Path("kontoId") int kontoId
    );

    // Pobieranie pojedynczej transakcji (jeśli potrzebne)
    @GET("api/transakcje/{transakcjaId}")
    Call<TransakcjaOdpowiedz> getTransakcjaById(
            @Header("Authorization") String authToken,
            @Path("transakcjaId") int transakcjaId
    );

    // Dodawanie nowej transakcji
    @POST("api/konta/{kontoId}/transakcje")
    Call<TransakcjaOdpowiedz> dodajTransakcje(
            @Header("Authorization") String authToken,
            @Path("kontoId") int kontoId,
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
    // Pobieranie transakcji dla konkretnego konta (ISTNIEJĄCA - może pozostać lub być usunięta jeśli nie jest już potrzebna)

    // NOWA METODA: Pobieranie transakcji dla konkretnego konta i okresu
    @GET("api/konta/{kontoId}/transakcje/okres") // Upewnij się, że ścieżka jest poprawna
    Call<List<TransakcjaOdpowiedz>> getTransakcjeDlaOkresu(
            @Header("Authorization") String authToken,
            @Path("kontoId") int kontoId,
            @Query("dataOd") String dataOd,     // Parametr zapytania dla daty początkowej
            @Query("dataDo") String dataDo        // Parametr zapytania dla daty końcowej
    );
    //kategorie
    @GET("api/kategorie")
    Call<List<KategoriaOdpowiedz>> getKategorie(@Header("Authorization") String authToken);

}
