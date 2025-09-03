package com.example.zarzdzanie_finansami.dto.budzet;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class RegulaProcentowaDTO {
    @SerializedName("procentNaPotrzeby")
    private BigDecimal procentNaPotrzeby;
    @SerializedName("procentNaZachcianki")
    private BigDecimal procentNaZachcianki;
    @SerializedName("procentNaInwestycje")
    private BigDecimal procentNaInwestycje;
    @SerializedName("zastosuj")
    private boolean zastosuj;

    public BigDecimal getProcentNaPotrzeby() {
        return procentNaPotrzeby;
    }

    public void setProcentNaPotrzeby(BigDecimal procentNaPotrzeby) {
        this.procentNaPotrzeby = procentNaPotrzeby;
    }

    public BigDecimal getProcentNaZachcianki() {
        return procentNaZachcianki;
    }

    public void setProcentNaZachcianki(BigDecimal procentNaZachcianki) {
        this.procentNaZachcianki = procentNaZachcianki;
    }

    public BigDecimal getProcentNaInwestycje() {
        return procentNaInwestycje;
    }

    public void setProcentNaInwestycje(BigDecimal procentNaInwestycje) {
        this.procentNaInwestycje = procentNaInwestycje;
    }

    public boolean isZastosuj() {
        return zastosuj;
    }

    public void setZastosuj(boolean zastosuj) {
        this.zastosuj = zastosuj;
    }
}
