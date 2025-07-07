package com.example.zarzdzanie_finansami.dto;

import java.math.BigDecimal;

public class TransakcjaWysylanie {
    private String opis;
    private BigDecimal kwota;
    private String data;
    private String typ;
    private Integer kategoriaId;
    private int kontoId;

    public TransakcjaWysylanie(String opis, BigDecimal kwota, String data, String typ, Integer kategoriaId, int kontoId) {
        this.opis = opis;
        this.kwota = kwota;
        this.data = data;
        this.typ = typ;
        this.kategoriaId = kategoriaId;
        this.kontoId = kontoId;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public BigDecimal getKwota() {
        return kwota;
    }

    public void setKwota(BigDecimal kwota) {
        this.kwota = kwota;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public Integer getKategoriaId() {
        return kategoriaId;
    }

    public void setKategoriaId(Integer kategoriaId) {
        this.kategoriaId = kategoriaId;
    }

    public int getKontoId() {
        return kontoId;
    }

    public void setKontoId(int kontoId) {
        this.kontoId = kontoId;
    }
}
