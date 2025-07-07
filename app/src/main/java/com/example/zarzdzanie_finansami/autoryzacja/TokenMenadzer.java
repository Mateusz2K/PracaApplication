package com.example.zarzdzanie_finansami.autoryzacja;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TokenMenadzer {
    private static final String SHARED_PREF_NAME = "app_shared_pref";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_LAST_ACTIVE_TIME = "last_active_time"; // Nowy klucz
    private static final long MAX_INACTIVE_INTERVAL_MS = 15 * 60 * 1000; // 15 minut w milisekundach

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public TokenMenadzer(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.putLong(KEY_LAST_ACTIVE_TIME, System.currentTimeMillis()); // Zapisz czas przy zapisie tokenu
        editor.apply();
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }

    public void clearAuthToken() {
        editor.remove(KEY_AUTH_TOKEN);
        editor.remove(KEY_LAST_ACTIVE_TIME); // Usuń również czas
        editor.apply();
        Log.d("TokenMenadzer", "Token usunięty");
    }

    public boolean hasToken() {
        return getAuthToken() != null;
    }

    // Nowa metoda do aktualizacji czasu aktywności
    public void updateLastActiveTime() {
        if (hasToken()) { // Aktualizuj tylko jeśli token istnieje
            editor.putLong(KEY_LAST_ACTIVE_TIME, System.currentTimeMillis());
            editor.apply();
        }
    }

    // Nowa metoda do sprawdzania, czy sesja wygasła
    public boolean isSessionExpired() {
        long lastActiveTime = sharedPreferences.getLong(KEY_LAST_ACTIVE_TIME, 0);
        if (lastActiveTime == 0) { // Jeśli nie ma zapisanego czasu, a token jest (co nie powinno się zdarzyć)
            return hasToken(); // Traktuj jako wygaśnięty, jeśli jest token
        }
        return (System.currentTimeMillis() - lastActiveTime) > MAX_INACTIVE_INTERVAL_MS;
    }
}