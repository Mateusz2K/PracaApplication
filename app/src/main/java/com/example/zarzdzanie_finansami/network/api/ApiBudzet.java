package com.example.zarzdzanie_finansami.network.api;

import com.example.zarzdzanie_finansami.dto.budzet.BudzetOdpowiedz;
import com.example.zarzdzanie_finansami.dto.budzet.BudzetWysylanie;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiBudzet{
        @GET("api/budzety")
        Call<List<BudzetOdpowiedz>> pobierzBudzety(@Header("Authorization") String authToken);

        @GET("api/budzety/{id}")
        Call<BudzetOdpowiedz> pobierzBudzet(@Header("Authorization") String authToken, @Path("id") Long budzetId);

        @POST("api/budzety")
        Call<BudzetOdpowiedz> dodajBudzet(@Header("Authorization") String authToken, @Body BudzetWysylanie budzetWysylanie);

        @DELETE("api/budzety/{id}")
        Call<Void> usunBudzet(@Header("Authorization") String authToken, @Path("id") Long budzetId);

        @PUT("api/budzety/{id}") // lub @PATCH, dostosuj endpoint i metodę HTTP
        Call<BudzetOdpowiedz> aktualizujBudzet(
                @Header("Authorization") String authToken,
                @Path("id") Long budzetId,
                @Body BudzetWysylanie budzetWysylanie
        );

}
