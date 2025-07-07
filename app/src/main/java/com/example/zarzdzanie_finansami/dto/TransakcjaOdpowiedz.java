package com.example.zarzdzanie_finansami.dto;


import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class TransakcjaOdpowiedz {

    @SerializedName("id")
    private int id;

    @SerializedName("opis")
    private String opis;

    @SerializedName("kwota")
    private BigDecimal kwota;

    @SerializedName("data") // Format daty np. "yyyy-MM-dd" lub "yyyy-MM-dd'T'HH:mm:ss"
    private String data;

    @SerializedName("typ") // Np. "KOSZT", "PRZYCHÓD"
    private String typ;

    @SerializedName("kategoria")
    private String kategoria; // Opcjonalnie

    @SerializedName("kontoId")
    private int kontoId;

    // Gettery (i Settery jeśli potrzebne)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    // Możesz dodać metodę toString() dla łatwiejszego debugowania
    @Override
    public String toString() {
        return "TransakcjaResponse{" +
                "id=" + id +
                ", opis='" + opis + '\'' +
                ", kwota=" + kwota +
                ", data='" + data + '\'' +
                ", typ='" + typ + '\'' +
                ", kategoria='" + kategoria + '\'' +
                ", kontoId=" + kontoId +
                '}';
    }
}
