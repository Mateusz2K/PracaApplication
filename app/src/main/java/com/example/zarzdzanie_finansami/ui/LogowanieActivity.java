package com.example.zarzdzanie_finansami.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zarzdzanie_finansami.MainActivity;
import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.logowanie.LogowanieWysylanie;
import com.example.zarzdzanie_finansami.dto.logowanie.LogowanieOdpowiedz;
import com.example.zarzdzanie_finansami.network.api.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;

import org.jetbrains.annotations.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogowanieActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private ApiSerwis apiService;
    private TokenMenadzer tokenManager;
    private static final String TAG = "LoginActivity";
    private TextView textViewRegisterLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegisterLink = findViewById(R.id.textViewRegisterLink);

        apiService = RetrofitKlient.getClient(this).create(ApiSerwis.class);
        tokenManager = new TokenMenadzer(this); // Inicjalizacja tokenManagera

        // Sprawdź, czy sesja wygasła PRZED sprawdzeniem, czy token istnieje
        if (tokenManager.hasToken() && tokenManager.isSessionExpired()) {
            tokenManager.clearAuthToken(); // Wyczyść token, jeśli sesja wygasła
            Toast.makeText(this, "Sesja wygasła. Zaloguj się ponownie.", Toast.LENGTH_SHORT).show();
        }

        if (tokenManager.hasToken()) {
            // Jeśli token nadal istnieje (i sesja nie wygasła), przejdź dalej
            tokenManager.updateLastActiveTime(); // Zaktualizuj czas aktywności
            startActivity(new Intent(LogowanieActivity.this, MainActivity.class));
            finish();
        }

        buttonLogin.setOnClickListener(v -> loginUser());
        textViewRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LogowanieActivity.this, RejestracjaActivity.class);
            startActivity(intent);
            // Nie kończ LogowanieActivity, aby użytkownik mógł wrócić, jeśli zrezygnuje z rejestracji
        });
    }

    private void loginUser() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Nazwa użytkownika i hasło są wymagane", Toast.LENGTH_SHORT).show();
            return;
        }

        LogowanieWysylanie loginRequest = new LogowanieWysylanie(username, password);
        Call<LogowanieOdpowiedz> call = apiService.logowanieUzytkownika(loginRequest);

        call.enqueue(new Callback<LogowanieOdpowiedz>() {
            @Override
            public void onResponse(Call<LogowanieOdpowiedz> call, Response<LogowanieOdpowiedz> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LogowanieOdpowiedz loginResponse = response.body();
                    tokenManager.saveAuthToken(loginResponse.getToken());
                    Toast.makeText(LogowanieActivity.this, "Zalogowano pomyślnie: " + loginResponse.getNazwa(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Token: " + loginResponse.getToken());
                    // Przejdź do następnej aktywności
                    startActivity(new Intent(LogowanieActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Obsługa błędu logowania (np. nieprawidłowe dane, błąd serwera)
                    String errorMessage = "Błąd logowania";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += ": " + response.code() + " " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Błąd parsowania errorBody", e);
                        }
                    } else if (response.message() != null) {
                        errorMessage += ": " + response.code() + " " + response.message();
                    }
                    Toast.makeText(LogowanieActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Błąd logowania: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<LogowanieOdpowiedz> call, Throwable t) {
                Toast.makeText(LogowanieActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Błąd sieci onFailure", t);
            }
        });
    }

}