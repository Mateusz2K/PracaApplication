package com.example.zarzdzanie_finansami.dto.logowanie;

public class RejestracjaOdpowiedz {
    private String nazwa;
    private String message; // Opcjonalny komunikat

    // Konstruktory, Gettery i Settery
    public RejestracjaOdpowiedz() {}

    public RejestracjaOdpowiedz( String nazwa, String message) {
        this.nazwa = nazwa;
        this.message = message;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}