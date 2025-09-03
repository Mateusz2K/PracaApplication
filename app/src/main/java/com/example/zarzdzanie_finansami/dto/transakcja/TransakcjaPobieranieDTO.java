package com.example.zarzdzanie_finansami.dto.transakcja;
import com.google.gson.annotations.SerializedName; // Jeśli używasz Gson z Retrofit

public class TransakcjaPobieranieDTO {

    @SerializedName("kategoriaId") // Użyj, jeśli nazwy pól w JSON różnią się od nazw pól w klasie
    private Integer kategoriaId;

    @SerializedName("kontoId")
    private Integer kontoId;

    @SerializedName("dataOd")
    private String dataOd; // Format "dd.MM.yyyy"

    @SerializedName("dataDo")
    private String dataDo; // Format "dd.MM.yyyy"

    @SerializedName("typTransakcji")
    private TypTransakcjiEnum typTransakcji;

    // Konstruktory
    public TransakcjaPobieranieDTO() {
        // Domyślny konstruktor potrzebny dla niektórych bibliotek (np. Gson)
    }

    public TransakcjaPobieranieDTO(Integer kontoId, Integer kategoriaId, String dataOd, String dataDo, TypTransakcjiEnum typTransakcji) {
        this.kontoId = kontoId;
        this.kategoriaId = kategoriaId;
        this.dataOd = dataOd;
        this.dataDo = dataDo;
        this.typTransakcji= typTransakcji;
    }

    // Gettery i Settery (lub użyj pól publicznych, jeśli preferujesz, choć gettery/settery są bardziej standardowe)

    public Integer getKategoriaId() {
        return kategoriaId;
    }

    public void setKategoriaId(Integer kategoriaId) {
        this.kategoriaId = kategoriaId;
    }

    public Integer getKontoId() {
        return kontoId;
    }

    public void setKontoId(Integer kontoId) {
        this.kontoId = kontoId;
    }

    public String getDataOd() {
        return dataOd;
    }

    public void setDataOd(String dataOd) {
        this.dataOd = dataOd;
    }

    public String getDataDo() {
        return dataDo;
    }

    public void setDataDo(String dataDo) {
        this.dataDo = dataDo;
    }
    public TypTransakcjiEnum getTypTransakcji() {
        return typTransakcji;
    }

    public void setTypTransakcji(TypTransakcjiEnum typTransakcji) {
        this.typTransakcji = typTransakcji;
    }

    @Override
    public String toString() {
        return "TransakcjaPobieranieDTO{" +
                "kategoriaId=" + kategoriaId +
                ", kontoId=" + kontoId +
                ", dataOd='" + dataOd + '\'' +
                ", dataDo='" + dataDo + '\'' +
                ", typTransakcji=" + typTransakcji +
                '}';
    }
}
