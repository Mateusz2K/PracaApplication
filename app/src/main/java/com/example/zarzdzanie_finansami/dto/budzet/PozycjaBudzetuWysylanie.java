package com.example.zarzdzanie_finansami.dto.budzet;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class PozycjaBudzetuWysylanie {
    @SerializedName("kategoriaId")
    private Long kategoriaId;
    @SerializedName("procentAlokowany")
    private BigDecimal procentAlokowany;
    @SerializedName("kwotaAlokowana")
    private BigDecimal kwotaAlokowana;
    @SerializedName("typAlokacji")
    private TypAlokacjiEnum typAlokacji;
}
