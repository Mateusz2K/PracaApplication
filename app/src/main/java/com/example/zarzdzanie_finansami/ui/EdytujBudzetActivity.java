package com.example.zarzdzanie_finansami.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Dodano Toolbar

import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.budzet.BudzetOdpowiedz;
import com.example.zarzdzanie_finansami.dto.budzet.BudzetWysylanie;
import com.example.zarzdzanie_finansami.dto.budzet.TypRegulyBudzetowejEnum;
import com.example.zarzdzanie_finansami.network.api.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EdytujBudzetActivity extends AppCompatActivity {

    private static final String TAG = "EdytujBudzetActivity";
    public static final String EXTRA_BUDZET_DO_EDYCJI = "BUDZET_DO_EDYCJI";

    private TextInputEditText editTextNazwa, editTextDataPoczatkowa, editTextDataKoncowa, editTextDochod;
    private Spinner spinnerTypReguly;
    private Button buttonSaveChanges; // Zmieniono nazwę przycisku

    private ApiSerwis apiService;
    private TokenMenadzer tokenManager;
    private Calendar calendar;
    private final SimpleDateFormat dateFormatterUi = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    // Formatter do parsowania daty z BudzetOdpowiedz, jeśli jest w innym formacie
    // private final SimpleDateFormat dateFormatterDto = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    private BudzetOdpowiedz budzetDoEdycji;
    private Long budzetIdDoEdycji;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Użyj tego samego layoutu lub stwórz nowy, dedykowany dla edycji
        setContentView(R.layout.activity_dodaj_budzet); // Załóżmy reużycie layoutu

        Toolbar toolbar = findViewById(R.id.toolbar_form_budzet); // Załóżmy, że toolbar ma takie ID w layoutcie
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edytuj Budżet");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Pokaż przycisk wstecz
        }


        tokenManager = new TokenMenadzer(this);
        apiService = RetrofitKlient.getClient(this).create(ApiSerwis.class);
        calendar = Calendar.getInstance();

        editTextNazwa = findViewById(R.id.editTextBudzetNazwa);
        editTextDataPoczatkowa = findViewById(R.id.editTextDataPoczatkowa);
        editTextDataKoncowa = findViewById(R.id.editTextDataKoncowa);
        editTextDochod = findViewById(R.id.editTextPrzewidywanyDochod);
        spinnerTypReguly = findViewById(R.id.spinnerTypReguly);
        buttonSaveChanges = findViewById(R.id.buttonSaveBudzet); // ID przycisku z layoutu dodawania
        buttonSaveChanges.setText("Zapisz Zmiany"); // Zmień tekst przycisku

        setupDatePickers();
        setupSpinner();

        if (getIntent().hasExtra(EXTRA_BUDZET_DO_EDYCJI)) {
            budzetDoEdycji = getIntent().getParcelableExtra(EXTRA_BUDZET_DO_EDYCJI);
            if (budzetDoEdycji != null) {
                budzetIdDoEdycji = budzetDoEdycji.getId();
                populateFields(budzetDoEdycji);
            } else {
                Toast.makeText(this, "Błąd ładowania danych budżetu.", Toast.LENGTH_SHORT).show();
                finish(); // Zakończ aktywność, jeśli nie ma danych
                return;
            }
        } else {
            Toast.makeText(this, "Nie przekazano budżetu do edycji.", Toast.LENGTH_SHORT).show();
            finish(); // Zakończ aktywność
            return;
        }

        buttonSaveChanges.setOnClickListener(v -> updateBudzet());
    }

    private void populateFields(BudzetOdpowiedz budzet) {
        editTextNazwa.setText(budzet.getNazwa());
        editTextDataPoczatkowa.setText(budzet.getDataPoczatkowa());
        editTextDataKoncowa.setText(budzet.getDataKoncowa());


        if (budzet.getPrzewidywanyDochod() != null) {
            editTextDochod.setText(budzet.getPrzewidywanyDochod().toPlainString());
        }

        if (budzet.getTypReguly() != null) {
            TypRegulyBudzetowejEnum typReguly = budzet.getTypReguly();
            ArrayAdapter<TypRegulyBudzetowejEnum> adapter = (ArrayAdapter<TypRegulyBudzetowejEnum>) spinnerTypReguly.getAdapter();
            if (adapter != null) {
                int position = adapter.getPosition(typReguly);
                if (position >= 0) {
                    spinnerTypReguly.setSelection(position);
                }
            }
        }
    }

    private void setupDatePickers() {
        editTextDataPoczatkowa.setOnClickListener(v -> showDatePickerDialog(editTextDataPoczatkowa));
        editTextDataKoncowa.setOnClickListener(v -> showDatePickerDialog(editTextDataKoncowa));
    }

    private void showDatePickerDialog(TextInputEditText editText) {
        // Spróbuj ustawić datę początkową w DatePicker na podstawie aktualnej wartości pola
        Calendar initialCalendar = Calendar.getInstance();
        try {
            if (!TextUtils.isEmpty(editText.getText())) {
                Date selectedDate = dateFormatterUi.parse(editText.getText().toString());
                if (selectedDate != null) {
                    initialCalendar.setTime(selectedDate);
                }
            }
        } catch (ParseException e) {
            Log.w(TAG, "Nie udało się sparsować daty z pola: " + editText.getText(), e);
            // Użyj bieżącej daty, jeśli parsowanie zawiedzie
        }


        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            editText.setText(dateFormatterUi.format(calendar.getTime()));
        };
        new DatePickerDialog(this, dateSetListener,
                initialCalendar.get(Calendar.YEAR),
                initialCalendar.get(Calendar.MONTH),
                initialCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupSpinner() {
        // Użyj Arrays.asList dla ArrayAdapter, jeśli chcesz mieć kontrolę nad listą
        ArrayAdapter<TypRegulyBudzetowejEnum> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList(TypRegulyBudzetowejEnum.values()));
        spinnerTypReguly.setAdapter(adapter);
    }

    private void updateBudzet() {
        String nazwa = editTextNazwa.getText().toString().trim();
        String dataPoczatkowa = editTextDataPoczatkowa.getText().toString().trim();
        String dataKoncowa = editTextDataKoncowa.getText().toString().trim();
        String dochodStr = editTextDochod.getText().toString().trim();

        if (budzetIdDoEdycji == null) {
            Toast.makeText(this, "Błąd: Brak ID budżetu do aktualizacji.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(nazwa) || TextUtils.isEmpty(dataPoczatkowa) || TextUtils.isEmpty(dataKoncowa)) {
            Toast.makeText(this, "Wszystkie pola (nazwa, daty) są wymagane.", Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal dochod = null; // Dochod może być opcjonalny przy edycji lub mieć wartość domyślną
        if (!TextUtils.isEmpty(dochodStr)) {
            try {
                dochod = new BigDecimal(dochodStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Nieprawidłowy format dochodu.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // Jeśli dochód jest wymagany, dodaj walidację
            // Toast.makeText(this, "Przewidywany dochód jest wymagany.", Toast.LENGTH_SHORT).show();
            // return;
            // Jeśli może być pusty (i np. serwer interpretuje to jako 0 lub null)
            dochod = budzetDoEdycji.getPrzewidywanyDochod(); // Użyj starej wartości lub ustaw na null/BigDecimal.ZERO
        }


        BudzetWysylanie budzetWysylanie = new BudzetWysylanie();
        // ID nie jest zazwyczaj częścią ciała żądania PUT, ale przekazywane w ścieżce URL
        budzetWysylanie.setNazwa(nazwa);
        budzetWysylanie.setDataPoczatkowa(dataPoczatkowa); // Upewnij się, że format daty jest oczekiwany przez API
        budzetWysylanie.setDataKoncowa(dataKoncowa);     // Upewnij się, że format daty jest oczekiwany przez API
        budzetWysylanie.setPrzewidywanyDochod(dochod);
        budzetWysylanie.setTypReguly((TypRegulyBudzetowejEnum) spinnerTypReguly.getSelectedItem());
        // TODO: Dodać obsługę reguł kwotowych i procentowych w zależności od wyboru w spinnerze (tak jak w DodajBudzetActivity)

        String token = tokenManager.getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Błąd autoryzacji. Zaloguj się ponownie.", Toast.LENGTH_SHORT).show();
            // Możesz przekierować do logowania
            return;
        }

        // Załóżmy, że metoda w ApiSerwis nazywa się aktualizujBudzet
        // i przyjmuje ID budżetu jako parametr ścieżki
        apiService.aktualizujBudzet("Bearer " + token, budzetIdDoEdycji, budzetWysylanie)
                .enqueue(new Callback<BudzetOdpowiedz>() { // Lub Call<Void> jeśli API nie zwraca obiektu po aktualizacji
                    @Override
                    public void onResponse(Call<BudzetOdpowiedz> call, Response<BudzetOdpowiedz> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(EdytujBudzetActivity.this, "Budżet zaktualizowany pomyślnie!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK); // Ustaw wynik na OK, aby poprzednia aktywność mogła odświeżyć listę
                            finish(); // Zakończ aktywność edycji
                        } else {
                            // TODO: Lepsza obsługa błędów, np. parsowanie response.errorBody()
                            Toast.makeText(EdytujBudzetActivity.this, "Błąd aktualizacji budżetu: " + response.code() + " - " + response.message() , Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Błąd aktualizacji, kod: " + response.code() + ", wiadomość: " + response.message());
                            try {
                                if (response.errorBody() != null) {
                                    Log.e(TAG, "Error body: " + response.errorBody().string());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Błąd odczytu error body", e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<BudzetOdpowiedz> call, Throwable t) {
                        Toast.makeText(EdytujBudzetActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Błąd sieci przy aktualizacji budżetu", t);
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Możesz dodać logikę pytania o zapisanie niezapisanych zmian przed wyjściem
        onBackPressed(); // Domyślne zachowanie przycisku wstecz
        return true;
    }
}
