package com.example.zarzdzanie_finansami.dto;

public class TransakcjaRequest {
    private String opis;
    private double kwota;
    private String data;
    private String typ;
    private String kategoria;
    private int kontoId;

    public TransakcjaRequest(String opis, double kwota, String data, String typ, String kategoria, int kontoId) {
        this.opis = opis;
        this.kwota = kwota;
        this.data = data;
        this.typ = typ;
        this.kategoria = kategoria;
        this.kontoId = kontoId;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public double getKwota() {
        return kwota;
    }

    public void setKwota(double kwota) {
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

    public String getKategoria() {
        return kategoria;
    }

    public void setKategoria(String kategoria) {
        this.kategoria = kategoria;
    }

    public int getKontoId() {
        return kontoId;
    }

    public void setKontoId(int kontoId) {
        this.kontoId = kontoId;
    }
}
