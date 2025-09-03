package com.example.zarzdzanie_finansami.ui;

public enum CelStatusEnumAndroid {
    AKTYWNY,
    ZAKOŃCZONY,
    PRZETERMINOWANY,
    NIEZNANY;
    public static CelStatusEnumAndroid fromString(String text) {
        if (text != null) {
            for (CelStatusEnumAndroid b : CelStatusEnumAndroid.values()) {
                if (text.equalsIgnoreCase(b.name())) {
                    return b;
                }
            }
        }
        return NIEZNANY;
    }
}
