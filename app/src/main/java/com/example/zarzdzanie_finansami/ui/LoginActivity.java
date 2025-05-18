package com.example.zarzdzanie_finansami.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.LogowanieRequest;
import com.example.zarzdzanie_finansami.dto.LogowanieResponse;
import com.example.zarzdzanie_finansami.network.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;

import org.jetbrains.annotations.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private ApiSerwis apiService;
    private TokenMenadzer tokenManager;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Załóżmy, że masz taki layout

        editTextUsername = findViewById(R.id.editTextUsername); // ID z Twojego layoutu
        editTextPassword = findViewById(R.id.editTextPassword); // ID z Twojego layoutu
        buttonLogin = findViewById(R.id.buttonLogin);       // ID z Twojego layoutu

        apiService = RetrofitKlient.getClient().create(ApiSerwis.class);
        tokenManager = new TokenMenadzer(this);

        // Jeśli użytkownik jest już zalogowany, przenieś go dalej
        if (tokenManager.hasToken()) {
            startActivity(new Intent(LoginActivity.this, KontaActivity.class));
            finish();
        }

        buttonLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Nazwa użytkownika i hasło są wymagane", Toast.LENGTH_SHORT).show();
            return;
        }

        LogowanieRequest loginRequest = new LogowanieRequest(username, password);
        Call<LogowanieResponse> call = apiService.loginUser(loginRequest);

        call.enqueue(new Callback<LogowanieResponse>() {
            @Override
            public void onResponse(Call<LogowanieResponse> call, Response<LogowanieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LogowanieResponse loginResponse = response.body();
                    tokenManager.saveAuthToken(loginResponse.getToken());
                    Toast.makeText(LoginActivity.this, "Zalogowano pomyślnie: " + loginResponse.getNazwa(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Token: " + loginResponse.getToken());
                    // Przejdź do następnej aktywności
                    startActivity(new Intent(LoginActivity.this, KontaActivity.class));
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
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Błąd logowania: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<LogowanieResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Błąd sieci onFailure", t);
            }
        });
    }

}