package com.example.zarzdzanie_finansami.dto.budzet;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class PozycjaBudzetuOdpowiedz implements Parcelable {
    @SerializedName("id")
    private Long id;

    @SerializedName("kategoriaId")
    private int kategoriaId;

    @SerializedName("kategoriaNazwa")
    private String kategoriaNazwa;

    @SerializedName("typAlokacji")
    private String typAlokacji;

    @SerializedName("procentAlokowany")
    private BigDecimal procentAlokowany;

    @SerializedName("kwotaAlokowana")
    private BigDecimal kwotaAlokowana;

    @SerializedName("rzeczywisteWydatki")
    private BigDecimal rzeczywisteWydatki;

    @SerializedName("pozostalo")
    private BigDecimal pozostalo;

    @SerializedName("procentWykorzystania")
    private double procentWykorzystania;

    // Gettery
    public Long getId() { return id; }
    public int getKategoriaId() { return kategoriaId; }
    public String getKategoriaNazwa() { return kategoriaNazwa; }
    public String getTypAlokacji() { return typAlokacji; } // Poprawiona nazwa
    public BigDecimal getProcentAlokowany() { return procentAlokowany; }
    public BigDecimal getKwotaAlokowana() { return kwotaAlokowana; }
    public BigDecimal getRzeczywisteWydatki() { return rzeczywisteWydatki; }
    public BigDecimal getPozostalo() { return pozostalo; }
    public double getProcentWykorzystania() { return procentWykorzystania; }

    // Konstruktor (może być wymagany przez Parcelable lub inne mechanizmy)
    public PozycjaBudzetuOdpowiedz() {}


    protected PozycjaBudzetuOdpowiedz(Parcel in) {
        id = in.readByte() == 0 ? null : in.readLong();
        kategoriaId = in.readInt();
        kategoriaNazwa = in.readString();
        typAlokacji = in.readString(); // Poprawiona nazwa

        String procentAlokowanyStr = in.readString();
        procentAlokowany = procentAlokowanyStr == null ? null : new BigDecimal(procentAlokowanyStr);

        String kwotaAlokowanaStr = in.readString();
        kwotaAlokowana = kwotaAlokowanaStr == null ? null : new BigDecimal(kwotaAlokowanaStr);

        String rzeczywisteWydatkiStr = in.readString();
        rzeczywisteWydatki = rzeczywisteWydatkiStr == null ? null : new BigDecimal(rzeczywisteWydatkiStr);

        String pozostaloStr = in.readString();
        pozostalo = pozostaloStr == null ? null : new BigDecimal(pozostaloStr);

        procentWykorzystania = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeInt(kategoriaId);
        dest.writeString(kategoriaNazwa);
        dest.writeString(typAlokacji); // Poprawiona nazwa

        dest.writeString(procentAlokowany == null ? null : procentAlokowany.toPlainString());
        dest.writeString(kwotaAlokowana == null ? null : kwotaAlokowana.toPlainString());
        dest.writeString(rzeczywisteWydatki == null ? null : rzeczywisteWydatki.toPlainString());
        dest.writeString(pozostalo == null ? null : pozostalo.toPlainString());
        dest.writeDouble(procentWykorzystania);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PozycjaBudzetuOdpowiedz> CREATOR = new Creator<PozycjaBudzetuOdpowiedz>() {
        @Override
        public PozycjaBudzetuOdpowiedz createFromParcel(Parcel in) {
            return new PozycjaBudzetuOdpowiedz(in);
        }

        @Override
        public PozycjaBudzetuOdpowiedz[] newArray(int size) {
            return new PozycjaBudzetuOdpowiedz[size];
        }
    };
}
