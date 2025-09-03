package com.example.zarzdzanie_finansami.dto.logowanie;

import com.google.gson.annotations.SerializedName;

public class LogowanieWysylanie {
    @SerializedName("nazwa")
    private String nazwa;
    @SerializedName("hasło")
    private String hasło;

    public LogowanieWysylanie(String nazwa, String hasło) {
        this.hasło = hasło;
        this.nazwa = nazwa;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getHasło() {
        return hasło;
    }

    public void setHasło(String hasło) {
        this.hasło = hasło;
    }
}
