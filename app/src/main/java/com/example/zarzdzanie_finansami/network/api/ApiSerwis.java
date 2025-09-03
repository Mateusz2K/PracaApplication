package com.example.zarzdzanie_finansami.network.api;


import com.example.zarzdzanie_finansami.dto.kategoria.KategoriaOdpowiedz;
import com.example.zarzdzanie_finansami.dto.transakcja.TransakcjaWysylanie;
import com.example.zarzdzanie_finansami.dto.transakcja.TransakcjaOdpowiedz;

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

public interface ApiSerwis extends ApiLogowanie, ApiKonto, ApiTransakcje, ApiBudzet, ApiCel, ApiKategorie {










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
