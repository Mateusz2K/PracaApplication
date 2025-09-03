package com.example.zarzdzanie_finansami.network.api;

import com.example.zarzdzanie_finansami.dto.kategoria.KategoriaOdpowiedz;
import com.example.zarzdzanie_finansami.dto.kategoria.KategoriaWysylanie;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiKategorie {
    @GET("api/kategorie")
    Call<List<KategoriaOdpowiedz>> pobierzWszystkieKategorie(@Header("Authorization") String authToken);

    // Wersja 2: Pobieranie kategorii z filtrowaniem po typie przez parametr URL
    @GET("api/kategorie")
    Call<List<KategoriaOdpowiedz>> pobierzKategoriePoTypie(
            @Header("Authorization") String authToken,
            @Query("typ") String typ // np. "PRZYCHOD" lub "KOSZT"
    );

    // Dodawanie nowej kategorii
    @POST("api/kategorie")
    Call<KategoriaOdpowiedz> dodajKategorie(
            @Header("Authorization") String authToken,
            @Body KategoriaWysylanie kategoriaDTO
    );

    // Modyfikacja istniejącej kategorii
    @PUT("api/kategorie/{kategoriaId}")
    Call<KategoriaOdpowiedz> modyfikujKategorie(
            @Header("Authorization") String authToken,
            @Path("kategoriaId") int kategoriaId,
            @Body KategoriaWysylanie kategoriaDTO
    );

    // Usuwanie kategorii
    @DELETE("api/kategorie/{kategoriaId}")
    Call<Void> usunKategorie(
            @Header("Authorization") String authToken,
            @Path("kategoriaId") int kategoriaId
    );
}
