package com.example.zarzdzanie_finansami.network.api;

import com.example.zarzdzanie_finansami.dto.konto.KontoOdpowiedz;
import com.example.zarzdzanie_finansami.dto.konto.KontoWysylanie;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiKonto {
    @GET("api/konta")
    Call<List<KontoOdpowiedz>> przeslijMojeKonta(@Header("Authorization") String authToken);

    @GET("api/konta/{id}")
    Call<KontoOdpowiedz> przeslijKontoOdId(@Header("Authorization") String authToken, @Path("id") int kontoId);

    @POST("api/konta")
    Call<KontoOdpowiedz> dodajKonto(
            @Header("Authorization") String authToken,
            @Body KontoWysylanie kontoWysylanie
    );

    @PUT("api/konta/{kontoId}")
    Call<KontoOdpowiedz> zmienKonto(
            @Header("Authorization") String authToken,
            @Path("kontoId") int kontoId,
            @Body KontoWysylanie kontoWysylanie
    );

    @DELETE("api/konta/{kontoId}")
    Call<Void> usunKonto(
            @Header("Authorization") String authToken,
            @Path("kontoId") int kontoId
    );

}
