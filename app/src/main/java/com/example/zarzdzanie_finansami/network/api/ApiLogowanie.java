package com.example.zarzdzanie_finansami.network.api;

import com.example.zarzdzanie_finansami.dto.logowanie.LogowanieOdpowiedz;
import com.example.zarzdzanie_finansami.dto.logowanie.LogowanieWysylanie;
import com.example.zarzdzanie_finansami.dto.logowanie.RejestracjaOdpowiedz;
import com.example.zarzdzanie_finansami.dto.logowanie.RejestracjaWysylanie;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiLogowanie {
    @POST("api/auth/login")
    Call<LogowanieOdpowiedz> logowanieUzytkownika(@Body LogowanieWysylanie loginRequest);

    @POST("api/rejestracja/rejestruj") // Upewnij się, że endpoint jest poprawny
    Call<RejestracjaOdpowiedz> zarejestrujUzytkownika(@Body RejestracjaWysylanie rejestracjaWysylanie);

}
