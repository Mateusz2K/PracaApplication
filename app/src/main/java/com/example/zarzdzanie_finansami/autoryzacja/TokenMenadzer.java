package com.example.zarzdzanie_finansami.autoryzacja;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenMenadzer {
    private static final String SHARED_PREF_NAME = "app_shared_pref";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public TokenMenadzer(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }

    public void clearAuthToken() {
        editor.remove(KEY_AUTH_TOKEN);
        editor.apply();
    }

    public boolean hasToken() {
        return getAuthToken() != null;
    }
}
