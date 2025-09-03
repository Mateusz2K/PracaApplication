package com.example.zarzdzanie_finansami.dto.budzet;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class BudzetWysylanie {

    @SerializedName("nazwa")
    private String nazwa;

    @SerializedName("dataPoczatkowa")
    private String dataPoczatkowa;

    @SerializedName("dataKoncowa")
    private String dataKoncowa;

    @SerializedName("okresowosc")
    private String okresowosc;

    @SerializedName("przewidywanyDochod")
    private BigDecimal przewidywanyDochod;

    @SerializedName("sumaAlokowana")
    private BigDecimal sumaAlokowana;
    @SerializedName("pozycjeBudzetu")
    private List<PozycjaBudzetuWysylanie> pozycjeBudzetu;
    @SerializedName("typReguly")
    private TypRegulyBudzetowejEnum typReguly;

    @SerializedName("regulaProcentowa")
    private RegulaProcentowaDTO regulaProcentowa;
    @SerializedName("regulaKwotowa")
    private RegulaKwotowaDTO regulaKwotowa;

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public void setDataPoczatkowa(String dataPoczatkowa) {
        this.dataPoczatkowa = dataPoczatkowa;
    }

    public void setDataKoncowa(String dataKoncowa) {
        this.dataKoncowa = dataKoncowa;
    }

    public void setOkresowosc(String okresowosc) {
        this.okresowosc = okresowosc;
    }

    public void setPrzewidywanyDochod(BigDecimal przewidywanyDochod) {
        this.przewidywanyDochod = przewidywanyDochod;
    }

    public void setSumaAlokowana(BigDecimal sumaAlokowana) {
        this.sumaAlokowana = sumaAlokowana;
    }

    public void setPozycjeBudzetu(List<PozycjaBudzetuWysylanie> pozycjeBudzetu) {
        this.pozycjeBudzetu = pozycjeBudzetu;
    }

    public void setTypReguly(TypRegulyBudzetowejEnum typReguly) {
        this.typReguly = typReguly;
    }

    public void setRegulaProcentowa(RegulaProcentowaDTO regulaProcentowa) {
        this.regulaProcentowa = regulaProcentowa;
    }

    public void setRegulaKwotowa(RegulaKwotowaDTO regulaKwotowa) {
        this.regulaKwotowa = regulaKwotowa;
    }
}
