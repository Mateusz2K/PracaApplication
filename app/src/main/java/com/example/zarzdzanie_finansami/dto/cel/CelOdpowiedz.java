package com.example.zarzdzanie_finansami.dto.cel;

import com.example.zarzdzanie_finansami.ui.CelStatusEnumAndroid;
import com.example.zarzdzanie_finansami.ui.CeleActivity;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.time.LocalDate;


public class CelOdpowiedz {
    @SerializedName("id")
    private Integer id;
    @SerializedName("nazwaCelu")
    private String nazwaCelu;
    @SerializedName("kwotaDocelowa")
    private BigDecimal kwotaDocelowa;
    @SerializedName("aktualnaKwota")
    private BigDecimal aktualnaKwota;
    @SerializedName("dataRozpoczecia")
    private LocalDate dataRozpoczecia;
    @SerializedName("dataZakonczenia")
    private LocalDate dataZakonczenia;
    @SerializedName("opis")
    private String opis;
    @SerializedName("uzytkownikId")
    private Integer uzytkownikId;
    @SerializedName("kontoId")
    private int kontoId;
    @SerializedName("procentOsiagniety")
    private double procentOsiagniety;
    @SerializedName("status")
    private CelStatusEnumAndroid status;

    public CelOdpowiedz(Integer id, String nazwaCelu, BigDecimal kwotaDocelowa, BigDecimal aktualnaKwota,
                        LocalDate dataRozpoczecia, LocalDate dataZakonczenia, String opis, Integer uzytkownikId,
                        int kontoId, CelStatusEnumAndroid status) { // Zmienione parametry
        this.id = id;
        this.nazwaCelu = nazwaCelu;
        this.kwotaDocelowa = kwotaDocelowa;
        this.aktualnaKwota = aktualnaKwota;
        this.dataRozpoczecia = dataRozpoczecia;
        this.dataZakonczenia = dataZakonczenia;
        this.opis = opis;
        this.uzytkownikId = uzytkownikId;
        this.kontoId = kontoId; // Inicjalizacja nowego pola
        this.status = status;   // Inicjalizacja nowego pola

        if (kwotaDocelowa != null && kwotaDocelowa.compareTo(BigDecimal.ZERO) > 0 && aktualnaKwota != null) {
            this.procentOsiagniety = aktualnaKwota.multiply(BigDecimal.valueOf(100)).divide(kwotaDocelowa, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        } else {
            this.procentOsiagniety = 0.0;
        }
    }
    public CelOdpowiedz() {
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getNazwaCelu() {
        return nazwaCelu;
    }

    public void setNazwaCelu(String nazwaCelu) {
        this.nazwaCelu = nazwaCelu;
    }

    public BigDecimal getKwotaDocelowa() {
        return kwotaDocelowa;
    }

    public void setKwotaDocelowa(BigDecimal kwotaDocelowa) {
        this.kwotaDocelowa = kwotaDocelowa;
    }

    public BigDecimal getAktualnaKwota() {
        return aktualnaKwota;
    }

    public void setAktualnaKwota(BigDecimal aktualnaKwota) {
        this.aktualnaKwota = aktualnaKwota;
    }

    public LocalDate getDataRozpoczecia() {
        return dataRozpoczecia;
    }

    public void setDataRozpoczecia(LocalDate dataRozpoczecia) {
        this.dataRozpoczecia = dataRozpoczecia;
    }

    public LocalDate getDataZakonczenia() {
        return dataZakonczenia;
    }

    public void setDataZakonczenia(LocalDate dataZakonczenia) {
        this.dataZakonczenia = dataZakonczenia;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public Integer getUzytkownikId() {
        return uzytkownikId;
    }

    public void setUzytkownikId(Integer uzytkownikId) {
        this.uzytkownikId = uzytkownikId;
    }

    public int getKontoId() {
        return kontoId;
    }

    public void setKontoId(int kontoId) {
        this.kontoId = kontoId;
    }

    public double getProcentOsiagniety() {
        return procentOsiagniety;
    }

    public void setProcentOsiagniety(double procentOsiagniety) {
        this.procentOsiagniety = procentOsiagniety;
    }

    public CelStatusEnumAndroid getStatus() {
        return status;
    }

    public void setStatus(CelStatusEnumAndroid status) {
        this.status = status;
    }
}
