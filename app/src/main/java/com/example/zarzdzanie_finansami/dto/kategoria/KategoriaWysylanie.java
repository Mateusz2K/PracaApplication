package com.example.zarzdzanie_finansami.dto.kategoria;

import com.example.zarzdzanie_finansami.dto.transakcja.TypTransakcjiEnum;
import com.google.gson.annotations.SerializedName;

public class KategoriaWysylanie {
    @SerializedName("nazwa")
    private String nazwa;
    @SerializedName("typTransakcji")
    private TypTransakcjiEnum typTransakcji;

    public KategoriaWysylanie(String nazwa, TypTransakcjiEnum typTransakcji) {
        this.nazwa = nazwa;
        this.typTransakcji = typTransakcji;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public void setTypTransakcji(TypTransakcjiEnum typTransakcji) {
        this.typTransakcji = typTransakcji;
    }
}
