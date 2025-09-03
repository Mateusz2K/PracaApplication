package com.example.zarzdzanie_finansami.dto.kategoria;

import androidx.annotation.NonNull;

import com.example.zarzdzanie_finansami.dto.transakcja.TypTransakcjiEnum;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class KategoriaOdpowiedz {

    @SerializedName("id")
    private int id;

    @SerializedName("nazwa")
    private String nazwa;

    // Zakładam, że serwer zwraca pole "typTransakcji" lub podobne,
    // które mapuje się na wartości "KOSZT" lub "PRZYCHÓD" (lub inne z Twojego TypTransakcjiEnum)
    @SerializedName("typTransakcji") // Użyj tej nazwy, jeśli tak zwraca serwer
    private TypTransakcjiEnum typTransakcji; // Zmieniona nazwa pola

    // Konstruktor, Gettery, Settery
    public KategoriaOdpowiedz(int id, String nazwa, TypTransakcjiEnum typTransakcji) {
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

    public TypTransakcjiEnum getTypTransakcji() { // Zmieniona nazwa metody
        return typTransakcji;
    }

    public void setTypTransakcji(TypTransakcjiEnum typTransakcji) { // Zmieniona nazwa metody
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
                (Objects.equals(nazwa, that.nazwa)) &&
                (Objects.equals(typTransakcji, that.typTransakcji));
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (nazwa != null ? nazwa.hashCode() : 0);
        result = 31 * result + (typTransakcji != null ? typTransakcji.hashCode() : 0);
        return result;
    }
}