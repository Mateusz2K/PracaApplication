package com.example.zarzdzanie_finansami.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class KontoWysylanie {

    @SerializedName("nazwa")
    private String nazwa;

    @SerializedName("typ")
    private String typ;

    @SerializedName("waluta")
    private String waluta; // Opcjonalnie, jeśli można ustawić przy tworzeniu/modyfikacji

    @SerializedName("bilans")
    private BigDecimal bilans; // Opcjonalnie, np. dla bilansu początkowego

    // Gettery i Settery
    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public String getWaluta() {
        return waluta;
    }

    public void setWaluta(String waluta) {
        this.waluta = waluta;
    }

    public BigDecimal getBilans() {
        return bilans;
    }

    public void setBilans(BigDecimal bilans) {
        this.bilans = bilans;
    }
}
