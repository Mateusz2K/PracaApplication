package com.example.zarzdzanie_finansami.network.api;

import retrofit2.Call;

import com.example.zarzdzanie_finansami.dto.cel.CelOdpowiedz;
import com.example.zarzdzanie_finansami.dto.cel.CelWysylanie;
import com.example.zarzdzanie_finansami.dto.cel.ZasilenieCelu;

import java.math.BigDecimal;
import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiCel {
    @POST("api/cele")
    Call<CelOdpowiedz> stworzCel(
            @Header("Authorization") String authToken,
            @Body CelWysylanie celDTO
    );

    @GET("api/cele")
    Call<List<CelOdpowiedz>> pobierzCeleUzytkownika(
            @Header("Authorization") String authToken
    );

    @GET("api/cele/{id}")
    Call<CelOdpowiedz> pobierzCelPoId(
            @Header("Authorization") String authToken,
            @Path("id") Integer celId
    );

    @PUT("api/cele/{id}")
    Call<CelOdpowiedz> aktualizujCel(
            @Header("Authorization") String authToken,
            @Path("id") Integer celId,
            @Body CelWysylanie celDTO
    );

    @DELETE("api/cele/{id}")
    Call<Void> usunCel(
            @Header("Authorization") String authToken,
            @Path("id") Integer celId
    );

    // Endpoint do dodawania środków do celu
    // Pamiętaj: ten endpoint w obecnej formie backendu nie jest powiązany z transferem z konta!
    @POST("api/cele/{id}/dodaj-srodki")
    Call<CelOdpowiedz> dodajSrodkiDoCelu(
            @Header("Authorization") String authToken,
            @Path("id") Integer celId,
            @Body ZasilenieCelu zasilenieDTO // ZMIANA: użyj @Body i nowego DTO
    );
}
