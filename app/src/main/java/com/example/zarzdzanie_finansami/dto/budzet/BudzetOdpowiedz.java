package com.example.zarzdzanie_finansami.dto.budzet;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BudzetOdpowiedz implements Parcelable {
    @SerializedName("id")
    private Long id;

    @SerializedName("nazwa")
    private String nazwa;

    @SerializedName("kwota")
    private double kwota;

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

    @SerializedName("sumaRzeczywistychWydatow")
    private BigDecimal sumaRzeczywistychWydatow;

    @SerializedName("saldoBudzetu")//dochod - sumaRzeczywistychWydatow lub sumaAlokowana - sumaRzeczywistychWydatow
    private BigDecimal saldoBudzetu;

    @SerializedName("aktywny")
    private boolean aktywny;

    @SerializedName("szablonId")
    private Integer szablonId;

    @SerializedName("typReguly")
    private TypRegulyBudzetowejEnum typReguly;

    @SerializedName("procentNaPotrzeby")
    private int procentNaPotrzeby;;

    @SerializedName("procentNaZachcianki")
    private int procentNaZachcianki;

    @SerializedName("procentNaInwestycje")
    private int procentNaInwestycje;

    @SerializedName("pozycjeBudzetu")
    private List<PozycjaBudzetuOdpowiedz> pozycjeBudzetu;

    public BudzetOdpowiedz() {
    }

    public Long getId() {
        return id;
    }

    public String getNazwa() {
        return nazwa;
    }

    public double getKwota() {
        return kwota;
    }

    public String getDataPoczatkowa() {
        return dataPoczatkowa;
    }

    public String getDataKoncowa() {
        return dataKoncowa;
    }

    public String getOkresowosc() {
        return okresowosc;
    }

    public BigDecimal getPrzewidywanyDochod() {
        return przewidywanyDochod;
    }

    public BigDecimal getSumaAlokowana() {
        return sumaAlokowana;
    }

    public BigDecimal getSumaRzeczywistychWydatow() {
        return sumaRzeczywistychWydatow;
    }

    public BigDecimal getSaldoBudzetu() {
        return saldoBudzetu;
    }

    public boolean isAktywny() {
        return aktywny;
    }

    public Integer getSzablonId() {
        return szablonId;
    }
    public TypRegulyBudzetowejEnum getTypReguly() {
        return typReguly;
    }

    public int getProcentNaPotrzeby() {
        return procentNaPotrzeby;
    }

    public int getProcentNaZachcianki() {
        return procentNaZachcianki;
    }

    public int getProcentNaInwestycje() {
        return procentNaInwestycje;
    }

    public List<PozycjaBudzetuOdpowiedz> getPozycjeBudzetu() {
        return pozycjeBudzetu;
    }

    @Override
    public String toString() {
        return "BudzetOdpowiedz{" +
                "id=" + id +
                ", nazwa='" + nazwa + '\'' +
                ", kwota=" + kwota +
                ", dataPoczatkowa='" + dataPoczatkowa + '\'' +
                ", dataKoncowa='" + dataKoncowa + '\'' +
                ", okresowosc='" + okresowosc + '\'' +
                ", przewidywanyDochod=" + przewidywanyDochod +
                ", sumaAlokowana=" + sumaAlokowana +
                ", sumaRzeczywistychWydatow=" + sumaRzeczywistychWydatow +
                ", saldoBudzetu=" + saldoBudzetu +
                ", aktywny=" + aktywny +
                ", szablonId=" + szablonId +
                ", procentNaPotrzeby=" + procentNaPotrzeby +
                ", procentNaZachcianki=" + procentNaZachcianki +
                ", procentNaInwestycje=" + procentNaInwestycje +
                ", pozycjeBudzetu=" + pozycjeBudzetu +
                '}';
    }

    // Implementacja Parcelable
    protected BudzetOdpowiedz(Parcel in) {
        id = in.readByte() == 0 ? null : in.readLong();
        nazwa = in.readString();
        kwota = in.readDouble(); // Zmieniona nazwa
        dataPoczatkowa = in.readString();
        dataKoncowa = in.readString();
        okresowosc = in.readString();
        // Dla BigDecimal: czytaj jako String i konwertuj
        String przewidywanyDochodStr = in.readString();
        przewidywanyDochod = przewidywanyDochodStr == null ? null : new BigDecimal(przewidywanyDochodStr);
        String sumaAlokowanaStr = in.readString();
        sumaAlokowana = sumaAlokowanaStr == null ? null : new BigDecimal(sumaAlokowanaStr);
        String sumaRzeczywistychWydatowStr = in.readString();
        sumaRzeczywistychWydatow = sumaRzeczywistychWydatowStr == null ? null : new BigDecimal(sumaRzeczywistychWydatowStr);
        String saldoBudzetuStr = in.readString();
        saldoBudzetu = saldoBudzetuStr == null ? null : new BigDecimal(saldoBudzetuStr);

        aktywny = in.readByte() != 0;
        szablonId = in.readInt(); // Dostosuj, jeśli może być null (czytaj jako obiekt Integer)
        procentNaPotrzeby = in.readInt();
        procentNaZachcianki = in.readInt();
        procentNaInwestycje = in.readInt();
        // Dla listy obiektów Parcelable:
        if (in.readByte() == 1) {
            pozycjeBudzetu = new ArrayList<>();
            in.readList(pozycjeBudzetu, PozycjaBudzetuOdpowiedz.class.getClassLoader());
        } else {
            pozycjeBudzetu = null;
        }
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(nazwa);
        dest.writeDouble(kwota); // Zmieniona nazwa
        dest.writeString(dataPoczatkowa);
        dest.writeString(dataKoncowa);
        dest.writeString(okresowosc);
        // Dla BigDecimal: zapisz jako String
        dest.writeString(przewidywanyDochod == null ? null : przewidywanyDochod.toPlainString());
        dest.writeString(sumaAlokowana == null ? null : sumaAlokowana.toPlainString());
        dest.writeString(sumaRzeczywistychWydatow == null ? null : sumaRzeczywistychWydatow.toPlainString());
        dest.writeString(saldoBudzetu == null ? null : saldoBudzetu.toPlainString());

        dest.writeByte((byte) (aktywny ? 1 : 0));
        dest.writeInt(szablonId); // Dostosuj dla null
        dest.writeInt(procentNaPotrzeby);
        dest.writeInt(procentNaZachcianki);
        dest.writeInt(procentNaInwestycje);
        // Dla listy obiektów Parcelable:
        if (pozycjeBudzetu == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeList(pozycjeBudzetu);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BudzetOdpowiedz> CREATOR = new Creator<>() {
        @Override
        public BudzetOdpowiedz createFromParcel(Parcel in) {
            return new BudzetOdpowiedz(in);
        }

        @Override
        public BudzetOdpowiedz[] newArray(int size) {
            return new BudzetOdpowiedz[size];
        }
    };
}
