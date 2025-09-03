package com.example.zarzdzanie_finansami.dto.budzet;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class RegulaKwotowaDTO {
    @SerializedName("kwotaNaPotrzeby")
    private BigDecimal kwotaNaPotrzeby;
    @SerializedName("kwotaNaZachcianki")
    private BigDecimal kwotaNaZachcianki;
    @SerializedName("kwotaNaInwestycje")
    private BigDecimal kwotaNaInwestycje;

    public BigDecimal getKwotaNaInwestycje() {
        return kwotaNaInwestycje;
    }

    public void setKwotaNaInwestycje(BigDecimal kwotaNaInwestycje) {
        this.kwotaNaInwestycje = kwotaNaInwestycje;
    }

    public BigDecimal getKwotaNaZachcianki() {
        return kwotaNaZachcianki;
    }

    public void setKwotaNaZachcianki(BigDecimal kwotaNaZachcianki) {
        this.kwotaNaZachcianki = kwotaNaZachcianki;
    }

    public BigDecimal getKwotaNaPotrzeby() {
        return kwotaNaPotrzeby;
    }

    public void setKwotaNaPotrzeby(BigDecimal kwotaNaPotrzeby) {
        this.kwotaNaPotrzeby = kwotaNaPotrzeby;
    }
}
