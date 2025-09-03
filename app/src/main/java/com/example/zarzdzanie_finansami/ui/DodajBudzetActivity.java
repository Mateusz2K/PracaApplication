package com.example.zarzdzanie_finansami.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.budzet.BudzetOdpowiedz;
import com.example.zarzdzanie_finansami.dto.budzet.BudzetWysylanie;
import com.example.zarzdzanie_finansami.dto.budzet.TypRegulyBudzetowejEnum;
import com.example.zarzdzanie_finansami.network.api.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DodajBudzetActivity extends AppCompatActivity {

    private TextInputEditText editTextNazwa, editTextDataPoczatkowa, editTextDataKoncowa, editTextDochod;
    private Spinner spinnerTypReguly;
    private Button buttonSave;

    private ApiSerwis apiService;
    private TokenMenadzer tokenManager;
    private Calendar calendar;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_budzet); // Upewnij się, że masz ten layout

        tokenManager = new TokenMenadzer(this);
        apiService = RetrofitKlient.getClient(this).create(ApiSerwis.class);
        calendar = Calendar.getInstance();

        editTextNazwa = findViewById(R.id.editTextBudzetNazwa);
        editTextDataPoczatkowa = findViewById(R.id.editTextDataPoczatkowa);
        editTextDataKoncowa = findViewById(R.id.editTextDataKoncowa);
        editTextDochod = findViewById(R.id.editTextPrzewidywanyDochod);
        spinnerTypReguly = findViewById(R.id.spinnerTypReguly);
        buttonSave = findViewById(R.id.buttonSaveBudzet);

        setupDatePickers();
        setupSpinner();

        buttonSave.setOnClickListener(v -> saveBudzet());
    }

    private void setupDatePickers() {
        editTextDataPoczatkowa.setOnClickListener(v -> showDatePickerDialog(editTextDataPoczatkowa));
        editTextDataKoncowa.setOnClickListener(v -> showDatePickerDialog(editTextDataKoncowa));
    }

    private void showDatePickerDialog(TextInputEditText editText) {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            editText.setText(dateFormatter.format(calendar.getTime()));
        };
        new DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupSpinner() {
        spinnerTypReguly.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TypRegulyBudzetowejEnum.values()));
    }

    private void saveBudzet() {
        String nazwa = editTextNazwa.getText().toString().trim();
        String dataPoczatkowa = editTextDataPoczatkowa.getText().toString().trim();
        String dataKoncowa = editTextDataKoncowa.getText().toString().trim();
        String dochodStr = editTextDochod.getText().toString().trim();

        if (TextUtils.isEmpty(nazwa) || TextUtils.isEmpty(dataPoczatkowa) || TextUtils.isEmpty(dataKoncowa)) {
            Toast.makeText(this, "Wszystkie pola są wymagane", Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal dochod;
        try {
            dochod = new BigDecimal(dochodStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Nieprawidłowy format dochodu", Toast.LENGTH_SHORT).show();
            return;
        }

        BudzetWysylanie budzetWysylanie = new BudzetWysylanie();
        budzetWysylanie.setNazwa(nazwa);
        budzetWysylanie.setDataPoczatkowa(dataPoczatkowa);
        budzetWysylanie.setDataKoncowa(dataKoncowa);
        budzetWysylanie.setPrzewidywanyDochod(dochod);
        budzetWysylanie.setTypReguly((TypRegulyBudzetowejEnum) spinnerTypReguly.getSelectedItem());
        // TODO: Dodać obsługę reguł kwotowych i procentowych w zależności od wyboru w spinnerze

        String token = tokenManager.getAuthToken();
        apiService.dodajBudzet("Bearer " + token, budzetWysylanie).enqueue(new Callback<BudzetOdpowiedz>() {
            @Override
            public void onResponse(Call<BudzetOdpowiedz> call, Response<BudzetOdpowiedz> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DodajBudzetActivity.this, "Budżet dodany pomyślnie!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(DodajBudzetActivity.this, "Błąd: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BudzetOdpowiedz> call, Throwable t) {
                Toast.makeText(DodajBudzetActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}