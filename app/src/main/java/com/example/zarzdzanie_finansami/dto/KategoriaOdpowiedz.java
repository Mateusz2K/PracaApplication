package com.example.zarzdzanie_finansami.dto;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

public class KategoriaOdpowiedz {

    @SerializedName("id")
    private int id;

    @SerializedName("nazwa")
    private String nazwa;

    // Zakładam, że serwer zwraca pole "typTransakcji" lub podobne,
    // które mapuje się na wartości "KOSZT" lub "PRZYCHOD" (lub inne z Twojego TypTransakcjiEnum)
    @SerializedName("typTransakcji") // Użyj tej nazwy, jeśli tak zwraca serwer
    private String typTransakcji; // Zmieniona nazwa pola

    // Konstruktor, Gettery, Settery
    public KategoriaOdpowiedz(int id, String nazwa, String typTransakcji) {
        this.id = id;
        this.nazwa = nazwa;
        this.typTransakcji = typTransakcji;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getTypTransakcji() { // Zmieniona nazwa metody
        return typTransakcji;
    }

    public void setTypTransakcji(String typTransakcji) { // Zmieniona nazwa metody
        this.typTransakcji = typTransakcji;
    }

    @NonNull
    @Override
    public String toString() {
        return nazwa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KategoriaOdpowiedz that = (KategoriaOdpowiedz) o;
        return id == that.id &&
                (nazwa != null ? nazwa.equals(that.nazwa) : that.nazwa == null) &&
                (typTransakcji != null ? typTransakcji.equals(that.typTransakcji) : that.typTransakcji == null);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (nazwa != null ? nazwa.hashCode() : 0);
        result = 31 * result + (typTransakcji != null ? typTransakcji.hashCode() : 0);
        return result;
    }
}