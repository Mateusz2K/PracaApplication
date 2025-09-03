package com.example.zarzdzanie_finansami.dto.logowanie;

import com.google.gson.annotations.SerializedName;

public class LogowanieOdpowiedz {
    @SerializedName("token") // Użyj @SerializedName jeśli nazwy pól w JSON różnią się od nazw pól w klasie
    private String token;

    @SerializedName("type")
    private String type;

    @SerializedName("nazwa")
    private String nazwa;

    // Gettery
    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getNazwa() {
        return nazwa;
    }
}
