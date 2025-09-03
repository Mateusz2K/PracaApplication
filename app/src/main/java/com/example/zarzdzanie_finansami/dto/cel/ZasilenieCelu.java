package com.example.zarzdzanie_finansami.dto.cel;

import java.math.BigDecimal;

public class ZasilenieCelu {
    private BigDecimal kwota;
    private Integer kontoZrodloweId; // Może być null

    public ZasilenieCelu(BigDecimal kwota, Integer kontoZrodloweId) {
        this.kwota = kwota;
        this.kontoZrodloweId = kontoZrodloweId;
    }

    public ZasilenieCelu(BigDecimal kwota) {
        this.kwota = kwota;
        this.kontoZrodloweId = null; // Domyślnie
    }

    // Gettery i Settery
    public BigDecimal getKwota() {
        return kwota;
    }

    public void setKwota(BigDecimal kwota) {
        this.kwota = kwota;
    }

    public Integer getKontoZrodloweId() {
        return kontoZrodloweId;
    }

    public void setKontoZrodloweId(Integer kontoZrodloweId) {
        this.kontoZrodloweId = kontoZrodloweId;
    }
}

