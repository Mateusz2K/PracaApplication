package com.example.zarzdzanie_finansami.dto.logowanie;

public class RejestracjaWysylanie {
    private String imie;
    private String nazwa; // Nazwa użytkownika
    private String email; // Email
    private String hasło; // Hasło

    public RejestracjaWysylanie(String imie,String nazwa, String email, String hasło) {
        this.imie = imie;
        this.nazwa = nazwa;
        this.email = email;
        this.hasło = hasło;
    }

    // Gettery są opcjonalne dla Retrofit, jeśli pola są publiczne,
    // ale dobra praktyka to ich dodanie lub użycie pól publicznych.
    // Jeśli Retrofit/Gson ma problemy, dodaj gettery.

    public String getImie() {
        return imie;
    }

    public String getNazwa() {
        return nazwa;
    }

    public String getEmail() {
        return email;
    }

    public String getHasło() {
        return hasło;
    }
}