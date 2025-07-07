package com.example.zarzdzanie_finansami.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.KategoriaOdpowiedz;
import com.example.zarzdzanie_finansami.dto.TransakcjaWysylanie;
import com.example.zarzdzanie_finansami.dto.TransakcjaOdpowiedz;
import com.example.zarzdzanie_finansami.network.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DodajTransakcjeActivity extends AppCompatActivity {

    private static final String TAG = "AddTransactionActivity";
    public static final String EXTRA_KONTO_ID = "extra_konto_id";
    public static final String EXTRA_KONTO_NAZWA = "extra_konto_nazwa";

    private TextInputEditText editTextOpis, editTextKwota, editTextData;
    private Spinner spinnerTyp, spinnerKategoria;
    private Button buttonSave;
    private TextView textViewTargetKonto;

    private ApiSerwis apiService;
    private TokenMenadzer tokenManager;
    private int kontoId;
    private String kontoNazwa;
    private Calendar calendar;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private List<KategoriaOdpowiedz> wszystkieKategorie = new ArrayList<>(); // Przechowuje wszystkie pobrane kategorie
    private List<KategoriaOdpowiedz> filtrowaneKategorie = new ArrayList<>(); // Kategorie po przefiltrowaniu
    private ArrayAdapter<KategoriaOdpowiedz> kategorieAdapter;
    private KategoriaOdpowiedz wybranaKategoria;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        Toolbar toolbar = findViewById(R.id.toolbar_add_transaction);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Dodaj Transakcję");
        }

        tokenManager = new TokenMenadzer(this);
        apiService = RetrofitKlient.getClient().create(ApiSerwis.class);
        calendar = Calendar.getInstance();

        kontoId = getIntent().getIntExtra(EXTRA_KONTO_ID, -1);
        kontoNazwa = getIntent().getStringExtra(EXTRA_KONTO_NAZWA);

        if (kontoId == -1) {
            Toast.makeText(this, "Nieprawidłowe ID konta.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Nie przekazano ID konta do AddTransactionActivity");
            finish();
            return;
        }

        editTextOpis = findViewById(R.id.editTextTransactionOpis);
        editTextKwota = findViewById(R.id.editTextTransactionKwota);
        editTextData = findViewById(R.id.editTextTransactionData);
        spinnerTyp = findViewById(R.id.spinnerTransactionTyp);
        spinnerKategoria = findViewById(R.id.spinnerTransactionKategoria);
        buttonSave = findViewById(R.id.buttonSaveTransaction);
        textViewTargetKonto = findViewById(R.id.textViewTargetKonto);

        if(kontoNazwa != null) {
            textViewTargetKonto.setText("Konto docelowe: " + kontoNazwa);
        } else {
            textViewTargetKonto.setText("Konto docelowe: ID " + kontoId);
        }

        setupSpinnerTyp(); // Spinner typów transakcji
        setupSpinnerKategorie(); // Spinner kategorii
        setupDatePicker();

        editTextData.setText(dateFormatter.format(calendar.getTime()));
        buttonSave.setOnClickListener(v -> saveTransaction());

        if (tokenManager.hasToken()) {
            fetchKategorie();
        } else {
            Toast.makeText(this, "Brak autoryzacji.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSpinnerTyp() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.typy_transakcji, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTyp.setAdapter(adapter);

        // Listener dla spinnera typów transakcji, aby filtrować kategorie
        spinnerTyp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String wybranyTyp = parent.getItemAtPosition(position).toString();
                filtrujKategoriePoTypie(wybranyTyp);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Można wyczyścić spinner kategorii lub zostawić bez zmian
                filtrowaneKategorie.clear();
                // Można dodać domyślną opcję "Najpierw wybierz typ transakcji"
                filtrowaneKategorie.add(new KategoriaOdpowiedz(0, "Wybierz typ transakcji...", null));
                kategorieAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setupSpinnerKategorie() {
        // Używamy filtrowaneKategorie dla adaptera
        kategorieAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filtrowaneKategorie);
        kategorieAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategoria.setAdapter(kategorieAdapter);

        spinnerKategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                wybranaKategoria = (KategoriaOdpowiedz) parent.getItemAtPosition(position);
                if (wybranaKategoria != null && wybranaKategoria.getId() == 0) { // ID 0 dla opcji "placeholder"
                    wybranaKategoria = null;
                }
                Log.d(TAG, "Wybrano kategorię: " + (wybranaKategoria != null ? wybranaKategoria.getNazwa() : "null"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                wybranaKategoria = null;
            }
        });
    }

    private void filtrujKategoriePoTypie(String typTransakcjiSpinner) {
        // Konwertuj wartość ze spinnera ("Koszt", "Przychód") na wartość oczekiwaną przez API/DTO ("KOSZT", "PRZYCHOD")
        String typApi;
        if (typTransakcjiSpinner.equalsIgnoreCase("Koszt")) {
            typApi = "KOSZT"; // Upewnij się, że te wartości są zgodne z tym, co jest w KategoriaResponse.typTransakcji
        } else if (typTransakcjiSpinner.equalsIgnoreCase("Przychód")) {
            typApi = "PRZYCHOD"; // lub "PRZYCHÓD"
        } else {
            typApi = null; // Nieznany typ, nie filtruj lub obsłuż inaczej
        }

        filtrowaneKategorie.clear();
        if (typApi != null) {
            for (KategoriaOdpowiedz kategoria : wszystkieKategorie) {
                // Porównujemy typ z KategoriaResponse z typem wybranym w spinnerze
                if (typApi.equalsIgnoreCase(kategoria.getTypTransakcji())) {
                    filtrowaneKategorie.add(kategoria);
                }
            }
        }

        if (filtrowaneKategorie.isEmpty()) {
            filtrowaneKategorie.add(new KategoriaOdpowiedz(0, "Brak kategorii dla typu", null)); // ID 0 jako placeholder
        }
        kategorieAdapter.notifyDataSetChanged();
        // Ustawienie domyślnego wyboru na pierwszą pozycję, jeśli lista nie jest pusta
        if (!filtrowaneKategorie.isEmpty() && spinnerKategoria != null) {
            spinnerKategoria.setSelection(0);
            wybranaKategoria = filtrowaneKategorie.get(0);
            if (wybranaKategoria != null && wybranaKategoria.getId() == 0) {
                wybranaKategoria = null;
            }
        } else {
            wybranaKategoria = null;
        }
    }


    private void fetchKategorie() {
        String token = tokenManager.getAuthToken();
        if (token == null) return;

        apiService.getKategorie("Bearer " + token).enqueue(new Callback<List<KategoriaOdpowiedz>>() {
            @Override
            public void onResponse(Call<List<KategoriaOdpowiedz>> call, Response<List<KategoriaOdpowiedz>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    wszystkieKategorie.clear();
                    wszystkieKategorie.addAll(response.body());
                    Log.d(TAG, "Pobrano wszystkich kategorii: " + wszystkieKategorie.size());
                    // Po pobraniu wszystkich kategorii, od razu filtruj na podstawie aktualnie wybranego typu transakcji
                    if (spinnerTyp.getSelectedItem() != null) {
                        filtrujKategoriePoTypie(spinnerTyp.getSelectedItem().toString());
                    } else {
                        // Jeśli nic nie jest wybrane w spinnerze typów, można wyświetlić domyślną listę lub pustą
                        filtrowaneKategorie.clear();
                        filtrowaneKategorie.add(new KategoriaOdpowiedz(0, "Wybierz typ transakcji...", null));
                        kategorieAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.e(TAG, "Błąd pobierania kategorii: " + response.code());
                    Toast.makeText(DodajTransakcjeActivity.this, "Nie udało się pobrać kategorii.", Toast.LENGTH_SHORT).show();
                    wszystkieKategorie.clear();
                    filtrowaneKategorie.clear();
                    filtrowaneKategorie.add(new KategoriaOdpowiedz(0, "Błąd pobierania kategorii", null));
                    kategorieAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<KategoriaOdpowiedz>> call, Throwable t) {
                Log.e(TAG, "Błąd sieci przy pobieraniu kategorii", t);
                Toast.makeText(DodajTransakcjeActivity.this, "Błąd sieci (kategorie): " + t.getMessage(), Toast.LENGTH_SHORT).show();
                wszystkieKategorie.clear();
                filtrowaneKategorie.clear();
                filtrowaneKategorie.add(new KategoriaOdpowiedz(0, "Błąd sieci", null));
                kategorieAdapter.notifyDataSetChanged();
            }
        });
    }


    private void setupDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            editTextData.setText(dateFormatter.format(calendar.getTime()));
        };

        editTextData.setOnClickListener(v -> new DatePickerDialog(DodajTransakcjeActivity.this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void saveTransaction() {
        String opis = editTextOpis.getText().toString().trim();
        String kwotaStr = editTextKwota.getText().toString().trim();
        String data = editTextData.getText().toString().trim();
        String typTransakcjiSpinner = spinnerTyp.getSelectedItem() != null ? spinnerTyp.getSelectedItem().toString() : null;

        Integer kategoriaIdDoWyslania = null;
        // Upewnij się, że wybranaKategoria nie jest obiektem placeholder (np. z ID 0)
        if (wybranaKategoria != null && wybranaKategoria.getId() != 0) {
            kategoriaIdDoWyslania = wybranaKategoria.getId();
        }

        if (TextUtils.isEmpty(opis) || TextUtils.isEmpty(kwotaStr) || TextUtils.isEmpty(data) || TextUtils.isEmpty(typTransakcjiSpinner)) {
            Toast.makeText(this, "Pola opis, kwota, data i typ są wymagane.", Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal kwota;
        try {
            kwota = new BigDecimal(kwotaStr.replace(',', '.')); // Zamień przecinek na kropkę dla BigDecimal
            if (kwota.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(this, "Kwota musi być większa od zera.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Nieprawidłowy format kwoty.", Toast.LENGTH_SHORT).show();
            return;
        }

        String apiTyp = "";
        if (typTransakcjiSpinner.equalsIgnoreCase("Koszt")) {
            apiTyp = "KOSZT";
        } else if (typTransakcjiSpinner.equalsIgnoreCase("Przychód")) {
            apiTyp = "PRZYCHOD"; // lub "PRZYCHÓD"
        }

        TransakcjaWysylanie transakcjaWysylanie = new TransakcjaWysylanie(opis, kwota, data, apiTyp, kategoriaIdDoWyslania, kontoId);
        String token = tokenManager.getAuthToken();

        if (token == null) {
            Toast.makeText(this, "Błąd autoryzacji. Zaloguj się ponownie.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.dodajTransakcje("Bearer " + token, kontoId, transakcjaWysylanie).enqueue(new Callback<TransakcjaOdpowiedz>() {
            @Override
            public void onResponse(Call<TransakcjaOdpowiedz> call, Response<TransakcjaOdpowiedz> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DodajTransakcjeActivity.this, "Transakcja dodana pomyślnie!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Brak szczegółów";
                        Log.e(TAG, "Błąd dodawania transakcji: " + response.code() + " - " + errorBody);
                        Toast.makeText(DodajTransakcjeActivity.this, "Błąd dodawania transakcji: " + response.code() + "\n" + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Błąd parsowania error body", e);
                        Toast.makeText(DodajTransakcjeActivity.this, "Błąd dodawania transakcji: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<TransakcjaOdpowiedz> call, Throwable t) {
                Log.e(TAG, "Błąd sieci przy dodawaniu transakcji", t);
                Toast.makeText(DodajTransakcjeActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}