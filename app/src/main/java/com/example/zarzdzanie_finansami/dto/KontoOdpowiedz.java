package com.example.zarzdzanie_finansami.dto;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal; // Pamiętaj o imporcie


public class KontoOdpowiedz {
    @SerializedName("id")
    private int id;

    @SerializedName("nazwa")
    private String nazwa;

    @SerializedName("bilans")
    private BigDecimal bilans;

    @SerializedName("typ")
    private String typ;

    @SerializedName("waluta")
    private String waluta;

    // Dla LocalDateTime z Gson mogą być potrzebne TypeAdaptery.
    // Dla uproszczenia można odebrać jako String i parsować ręcznie
    // lub skonfigurować Gson do obsługi LocalDateTime.
    // @SerializedName("dataUtworzenia")
    // private LocalDateTime dataUtworzenia;

    @SerializedName("dataUtworzenia")
    private String dataUtworzenia; // Prostsza opcja na start

    @SerializedName("uzytkownikId")
    private Integer uzytkownikId;

    // Gettery
    public int getId() { return id; }
    public String getNazwa() { return nazwa; }
    public BigDecimal getBilans() { return bilans; }
    public String getTyp() { return typ; }
    public String getWaluta() { return waluta; }
    public String getDataUtworzenia() { return dataUtworzenia; } // Jeśli jako String
    public Integer getUzytkownikId() { return uzytkownikId; }

    @Override
    public String toString() { // Przydatne do wyświetlania w ListView/RecyclerView
        return "Konto: " + nazwa + ", Bilans: " + bilans + " " + waluta;
    }
}
