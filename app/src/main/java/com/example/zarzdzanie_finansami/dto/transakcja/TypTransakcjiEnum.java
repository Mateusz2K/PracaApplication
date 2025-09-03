package com.example.zarzdzanie_finansami.dto.transakcja;

public enum TypTransakcjiEnum {
    KOSZT,
    PRZYCHÓD;

    public boolean equalsIgnoreCase(String aktualnieWybranyTyp) {
        return this.name().equalsIgnoreCase(aktualnieWybranyTyp);
    }
}
