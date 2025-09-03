package com.example.zarzdzanie_finansami.dto.konto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class KontoWysylanie {

    @SerializedName("nazwa")
    private String nazwa;

    @SerializedName("typ")
    private String typ;

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


    public BigDecimal getBilans() {
        return bilans;
    }

    public void setBilans(BigDecimal bilans) {
        this.bilans = bilans;
    }
}
