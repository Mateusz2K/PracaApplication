package com.example.zarzdzanie_finansami.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.dto.logowanie.RejestracjaOdpowiedz;
import com.example.zarzdzanie_finansami.dto.logowanie.RejestracjaWysylanie;
import com.example.zarzdzanie_finansami.network.api.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RejestracjaActivity extends AppCompatActivity {

    private static final String TAG = "RejestracjaActivity";

    private TextInputEditText editTextImie, editTextNazwa, editTextEmail, editTextHaslo, editTextPotwierdzHaslo;
    private Button buttonRegister;
    private TextView textViewLoginLink;

    private ApiSerwis apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rejestracja);

        editTextImie = findViewById(R.id.editTextRegisterImie);
        editTextNazwa = findViewById(R.id.editTextRegisterNazwa);
        editTextEmail = findViewById(R.id.editTextRegisterEmail);
        editTextHaslo = findViewById(R.id.editTextRegisterHaslo);
        editTextPotwierdzHaslo = findViewById(R.id.editTextRegisterPotwierdzHaslo);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLoginLink = findViewById(R.id.textViewLoginLink);

        apiService = RetrofitKlient.getClient(this).create(ApiSerwis.class);

        buttonRegister.setOnClickListener(v -> attemptRegistration());

        textViewLoginLink.setOnClickListener(v -> {
            // Przejdź z powrotem do LogowanieActivity
            Intent intent = new Intent(RejestracjaActivity.this, LogowanieActivity.class);
            startActivity(intent);
            finish(); // Zakończ RejestracjaActivity, aby nie wracać do niej przyciskiem "wstecz"
        });
    }

    private void attemptRegistration() {
        // Reset błędów
        editTextImie.setError(null);
        editTextNazwa.setError(null);
        editTextEmail.setError(null);
        editTextHaslo.setError(null);
        editTextPotwierdzHaslo.setError(null);

        String imie = editTextImie.getText().toString().trim();
        String nazwa = editTextNazwa.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String haslo = editTextHaslo.getText().toString().trim();
        String potwierdzHaslo = editTextPotwierdzHaslo.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Walidacja nazwy użytkownika
        if (TextUtils.isEmpty(nazwa)) {
            editTextNazwa.setError(getString(R.string.error_field_required));
            focusView = editTextNazwa;
            cancel = true;
        } else if (nazwa.length() < 3) {
            editTextNazwa.setError(getString(R.string.error_invalid_username_length));
            focusView = editTextNazwa;
            cancel = true;
        }

        // Walidacja emaila
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError(getString(R.string.error_field_required));
            focusView = editTextEmail;
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError(getString(R.string.error_invalid_email)); // Utwórz ten string
            focusView = editTextEmail;
            cancel = true;
        }

        // Walidacja hasła
        if (TextUtils.isEmpty(haslo)) {
            editTextHaslo.setError(getString(R.string.error_field_required));
            focusView = editTextHaslo;
            cancel = true;
        } else if (haslo.length() < 6) { // Przykładowa minimalna długość hasła
            editTextHaslo.setError(getString(R.string.error_invalid_password_length)); // Utwórz ten string
            focusView = editTextHaslo;
            cancel = true;
        }

        // Walidacja potwierdzenia hasła
        if (TextUtils.isEmpty(potwierdzHaslo)) {
            editTextPotwierdzHaslo.setError(getString(R.string.error_field_required));
            focusView = editTextPotwierdzHaslo;
            cancel = true;
        } else if (!haslo.equals(potwierdzHaslo)) {
            editTextPotwierdzHaslo.setError(getString(R.string.error_password_mismatch)); // Utwórz ten string
            focusView = editTextPotwierdzHaslo;
            cancel = true;
        }

        if (cancel) {
            // Był błąd, nie próbuj rejestracji, ustaw fokus na pierwszym błędnym polu
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            // Wszystkie dane są poprawne, wyślij żądanie rejestracji
            registerUser(imie,nazwa, email, haslo);
        }
    }

    private void registerUser(String imie, String nazwa, String email, String haslo) {
        RejestracjaWysylanie request = new RejestracjaWysylanie(imie, nazwa, email, haslo);
        Call<RejestracjaOdpowiedz> call = apiService.zarejestrujUzytkownika(request);

        call.enqueue(new Callback<RejestracjaOdpowiedz>() {
            @Override
            public void onResponse(Call<RejestracjaOdpowiedz> call, Response<RejestracjaOdpowiedz> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Rejestracja udana
                    RejestracjaOdpowiedz registrationResponse = response.body();
                    Toast.makeText(RejestracjaActivity.this,
                            "Rejestracja zakończona pomyślnie! " + (registrationResponse.getMessage() != null ? registrationResponse.getMessage() : ""),
                            Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Rejestracja udana dla: " + registrationResponse.getNazwa());

                    // Opcjonalnie: automatyczne logowanie po rejestracji lub przekierowanie do logowania
                    // Na razie przekierowujemy do logowania
                    Intent intent = new Intent(RejestracjaActivity.this, LogowanieActivity.class);
                    // Możesz dodać flagi, aby wyczyścić stos aktywności, jeśli chcesz
                    // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    // Błąd rejestracji
                    String errorMessage = "Błąd rejestracji";
                    if (response.errorBody() != null) {
                        try {
                            // Tutaj możesz spróbować sparsować bardziej szczegółowy błąd z serwera, jeśli go wysyła
                            // Np. jeśli serwer zwraca JSON z polem "error"
                            errorMessage += ": " + response.code() + " " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Błąd parsowania errorBody", e);
                        }
                    } else if (response.message() != null) {
                        errorMessage += ": " + response.code() + " " + response.message();
                    }
                    Toast.makeText(RejestracjaActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Błąd rejestracji: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<RejestracjaOdpowiedz> call, Throwable t) {
                // Błąd sieci lub inny problem z wykonaniem żądania
                Toast.makeText(RejestracjaActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Błąd sieci onFailure podczas rejestracji", t);
            }
        });
    }


}

