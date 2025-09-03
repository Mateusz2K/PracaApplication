package com.example.zarzdzanie_finansami.dto.cel;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class CelWysylanie {
    @SerializedName("nazwaCelu")
    private String nazwa;
    @SerializedName("kwotaDocelowa")
    private BigDecimal kwotaDocelowa;
    @SerializedName("dataZakonczenia")
    private String dataZakonczenia;
    @SerializedName("kontoId")
    private int kontoId;
    @SerializedName("opis")
    private String opis;
    @SerializedName("status")
    private String status;

    // Konstruktory
    public CelWysylanie(String nazwa, BigDecimal kwotaDocelowa, String dataZakonczenia, int kontoId, String opis) {
        this.nazwa = nazwa;
        this.kwotaDocelowa = kwotaDocelowa;
        this.dataZakonczenia = dataZakonczenia;
        this.kontoId = kontoId;
        this.opis = opis;
    }
    public CelWysylanie() {
    }

    // Gettery i Settery
    public String getNazwa() {
        return nazwa;
    }
    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }
    public BigDecimal getKwotaDocelowa() {
        return kwotaDocelowa;
    }
    public void setKwotaDocelowa(BigDecimal kwotaDocelowa) {
        this.kwotaDocelowa = kwotaDocelowa;
    }
    public String getDataZakonczenia() {
        return dataZakonczenia;
    }
    public void setDataZakonczenia(String dataZakonczenia) {
        this.dataZakonczenia = dataZakonczenia;
    }
    public int getKontoId() {
        return kontoId;
    }
    public void setKontoId(int kontoId) {
        this.kontoId = kontoId;
    }
    public String getOpis() {
        return opis;
    }
    public void setOpis(String opis) {
        this.opis = opis;
        }
}
