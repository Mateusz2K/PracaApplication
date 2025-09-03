package com.example.zarzdzanie_finansami.ui;// W TransakcjeActivity.java

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Załóżmy, że masz te importy lub podobne
import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.konto.KontoOdpowiedz;       // Załóżmy istnienie
import com.example.zarzdzanie_finansami.dto.kategoria.KategoriaOdpowiedz; // Załóżmy istnienie
import com.example.zarzdzanie_finansami.dto.transakcja.TransakcjaPobieranieDTO;
import com.example.zarzdzanie_finansami.dto.transakcja.TransakcjaOdpowiedz; // Dla adaptera RecyclerView
import com.example.zarzdzanie_finansami.dto.transakcja.TypTransakcjiEnum;
import com.example.zarzdzanie_finansami.network.api.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;
// import com.example.zarzdzanie_finansami.ui.Adaptery.TransakcjeAdapter; // Twój adapter

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransakcjeActivity extends AppCompatActivity {

    private static final String TAG = "TransakcjeActivity";
    public static final String EXTRA_KONTO_ID = "EXTRA_KONTO_ID"; // Stała dla ID konta

    private Toolbar toolbar;
    private RecyclerView recyclerViewTransactions;
    // private TransakcjeAdapter transakcjeAdapter; // Twój adapter
    private List<TransakcjaOdpowiedz> listaTransakcji = new ArrayList<>();
    private FloatingActionButton fabAddTransaction;

    private Button buttonPokazFiltry;
    private TextView textViewAktywneFiltry;

    private TransakcjaPobieranieDTO aktualneKryteriaFiltrowania = new TransakcjaPobieranieDTO();

    // Pola dla dialogu
    private Spinner spinnerTypTransakcjiDialog;
    private Spinner spinnerKontoDialog;
    private Spinner spinnerKategoriaDialog;
    private TextInputEditText editTextDataOdDialog;
    private TextInputEditText editTextDataDoDialog;

    // Listy i adaptery dla spinnerów w dialogu
    private List<KontoOdpowiedz> listaKontSpinner = new ArrayList<>();
    private List<KategoriaOdpowiedz> listaKategoriiSpinner = new ArrayList<>();
    private ArrayAdapter<String> adapterTypTransakcjiDialog;
    private ArrayAdapter<String> adapterKontoDialog;       // Będziemy przechowywać nazwy, a ID pobierać z listy
    private ArrayAdapter<String> adapterKategoriaDialog;   // Będziemy przechowywać nazwy, a ID pobierać z listy

    private TokenMenadzer tokenManager;
    private ApiSerwis apiService;

    private SimpleDateFormat dateFormatterUi = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private SimpleDateFormat backendDateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()); // Upewnij się, że format jest poprawny dla backendu


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transakcje);

        toolbar = findViewById(R.id.toolbar_transakcje);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Transakcje");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Pokaż przycisk Wstecz
        }

        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
        buttonPokazFiltry = findViewById(R.id.buttonPokazFiltry);
        textViewAktywneFiltry = findViewById(R.id.textViewAktywneFiltry);

        tokenManager = new TokenMenadzer(this);
        apiService = RetrofitKlient.getClient(this).create(ApiSerwis.class);

        setupRecyclerView();

        buttonPokazFiltry.setOnClickListener(v -> showFiltryDialog());
        fabAddTransaction.setOnClickListener(v -> {
            // Logika dodawania nowej transakcji
            // Intent intent = new Intent(this, DodajTransakcjeActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Dodaj transakcję (do implementacji)", Toast.LENGTH_SHORT).show();
        });

        // Pobierz dane dla spinnerów w tle
        fetchKontaForSpinner();
        fetchKategorieForSpinner();

        loadInitialData();
    }

    private void setupRecyclerView() {
        // transakcjeAdapter = new TransakcjeAdapter(this, listaTransakcji /*, listener */);
        // recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        // recyclerViewTransactions.setAdapter(transakcjeAdapter);
        // Tymczasowe, zastąp swoim adapterem
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
                return null;
            }
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {}
            @Override
            public int getItemCount() {
                return listaTransakcji.size();
            }
        });
    }


    private void loadInitialData() {
        if (getIntent().hasExtra(EXTRA_KONTO_ID)) {
            int kontoId = getIntent().getIntExtra(EXTRA_KONTO_ID, -1);
            if (kontoId != -1) {
                aktualneKryteriaFiltrowania.setKontoId(kontoId);
            }
        }
        pobierzIFiltrujTransakcje(aktualneKryteriaFiltrowania);
        updateAktywneFiltryTextView();
    }

    private void showFiltryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filtr_transakcja, null);
        builder.setView(dialogView);

        spinnerTypTransakcjiDialog = dialogView.findViewById(R.id.spinnerTypTransakcjiDialog);
        spinnerKontoDialog = dialogView.findViewById(R.id.spinnerKontoDialog);
        spinnerKategoriaDialog = dialogView.findViewById(R.id.spinnerKategoriaDialog);
        editTextDataOdDialog = dialogView.findViewById(R.id.editTextDataOdDialog);
        editTextDataDoDialog = dialogView.findViewById(R.id.editTextDataDoDialog);

        // Ustawianie focusable na false dla pól dat
        editTextDataOdDialog.setFocusable(false);
        editTextDataOdDialog.setClickable(true);
        editTextDataDoDialog.setFocusable(false);
        editTextDataDoDialog.setClickable(true);

        // Konfiguracja Spinnera Typu Transakcji
        List<String> typyTransakcji = new ArrayList<>(Arrays.asList("Wszystkie", "PRZYCHOD", "KOSZT"));
        adapterTypTransakcjiDialog = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typyTransakcji);
        spinnerTypTransakcjiDialog.setAdapter(adapterTypTransakcjiDialog);

        // Konfiguracja Spinnera Kont (używa nazw, ID są w listaKontSpinner)
        List<String> nazwyKont = new ArrayList<>();
        nazwyKont.add("Wszystkie konta"); // Pierwsza opcja
        for (KontoOdpowiedz konto : listaKontSpinner) {
            nazwyKont.add(konto.getNazwa());
        }
        adapterKontoDialog = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nazwyKont);
        spinnerKontoDialog.setAdapter(adapterKontoDialog);

        // Konfiguracja Spinnera Kategorii
        List<String> nazwyKategorii = new ArrayList<>();
        nazwyKategorii.add("Wszystkie kategorie"); // Pierwsza opcja
        for (KategoriaOdpowiedz kategoria : listaKategoriiSpinner) {
            nazwyKategorii.add(kategoria.getNazwa());
        }
        adapterKategoriaDialog = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nazwyKategorii);
        spinnerKategoriaDialog.setAdapter(adapterKategoriaDialog);

        // Ustaw aktualnie wybrane filtry w dialogu
        setInitialFilterValuesInDialog();

        // Ustawianie listenerów dla pól dat
        editTextDataOdDialog.setOnClickListener(v -> showDatePickerForDialog(editTextDataOdDialog));
        editTextDataDoDialog.setOnClickListener(v -> showDatePickerForDialog(editTextDataDoDialog));

        builder.setPositiveButton("Filtruj", (dialog, which) -> {
            aktualneKryteriaFiltrowania = new TransakcjaPobieranieDTO(); // Resetuj lub aktualizuj

            // Typ Transakcji
            int typPos = spinnerTypTransakcjiDialog.getSelectedItemPosition();
            if (typPos > 0) { // Pozycja 0 to "Wszystkie"
                aktualneKryteriaFiltrowania.setTypTransakcji(TypTransakcjiEnum.valueOf(typyTransakcji.get(typPos)));
            }

            // Konto
            int kontoPos = spinnerKontoDialog.getSelectedItemPosition();
            if (kontoPos > 0) { // Pozycja 0 to "Wszystkie konta"
                // Pobierz ID z listaKontSpinner (pozycja -1, bo dodaliśmy "Wszystkie konta")
                aktualneKryteriaFiltrowania.setKontoId(listaKontSpinner.get(kontoPos - 1).getId());
            }

            // Kategoria
            int kategoriaPos = spinnerKategoriaDialog.getSelectedItemPosition();
            if (kategoriaPos > 0) { // Pozycja 0 to "Wszystkie kategorie"
                aktualneKryteriaFiltrowania.setKategoriaId(listaKategoriiSpinner.get(kategoriaPos - 1).getId());
            }

            // Daty
            String dataOdStr = editTextDataOdDialog.getText().toString().trim();
            if (!dataOdStr.isEmpty()) {
                aktualneKryteriaFiltrowania.setDataOd(dataOdStr);
            }
            String dataDoStr = editTextDataDoDialog.getText().toString().trim();
            if (!dataDoStr.isEmpty()) {
                aktualneKryteriaFiltrowania.setDataDo(dataDoStr);
            }

            pobierzIFiltrujTransakcje(aktualneKryteriaFiltrowania);
            updateAktywneFiltryTextView();
            dialog.dismiss();
        });
        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton("Wyczyść filtry", (dialog, which) -> {
            aktualneKryteriaFiltrowania = new TransakcjaPobieranieDTO();
            // Jeśli aktywność jest dla konkretnego konta, zachowaj to ID
            if (getIntent().hasExtra(EXTRA_KONTO_ID)) {
                int kontoId = getIntent().getIntExtra(EXTRA_KONTO_ID, -1);
                if (kontoId != -1) {
                    aktualneKryteriaFiltrowania.setKontoId(kontoId);
                }
            }
            pobierzIFiltrujTransakcje(aktualneKryteriaFiltrowania);
            updateAktywneFiltryTextView();
            dialog.dismiss();
            Toast.makeText(this, "Filtry wyczyszczone.", Toast.LENGTH_SHORT).show();
        });

        builder.create().show();
    }

    private void setInitialFilterValuesInDialog() {
        // Typ transakcji
        if (aktualneKryteriaFiltrowania.getTypTransakcji() != null) {
            int typPosition = adapterTypTransakcjiDialog.getPosition(aktualneKryteriaFiltrowania.getTypTransakcji().toString());
            spinnerTypTransakcjiDialog.setSelection(typPosition);
        } else {
            spinnerTypTransakcjiDialog.setSelection(0); // "Wszystkie"
        }

        // Konto
        if (aktualneKryteriaFiltrowania.getKontoId() != null) {
            KontoOdpowiedz wybraneKonto = listaKontSpinner.stream()
                    .filter(k -> k.getId() ==(aktualneKryteriaFiltrowania.getKontoId()))
                    .findFirst().orElse(null);
            if (wybraneKonto != null) {
                int kontoPosition = adapterKontoDialog.getPosition(wybraneKonto.getNazwa());
                spinnerKontoDialog.setSelection(kontoPosition);
            } else {
                spinnerKontoDialog.setSelection(0); // "Wszystkie konta"
            }
        } else {
            spinnerKontoDialog.setSelection(0);
        }

        // Kategoria
        if (aktualneKryteriaFiltrowania.getKategoriaId() != null) {
            KategoriaOdpowiedz wybranaKategoria = listaKategoriiSpinner.stream()
                    .filter(k -> k.getId() == (aktualneKryteriaFiltrowania.getKategoriaId()))
                    .findFirst().orElse(null);
            if (wybranaKategoria != null) {
                int katPosition = adapterKategoriaDialog.getPosition(wybranaKategoria.getNazwa());
                spinnerKategoriaDialog.setSelection(katPosition);
            } else {
                spinnerKategoriaDialog.setSelection(0); // "Wszystkie kategorie"
            }
        } else {
            spinnerKategoriaDialog.setSelection(0);
        }

        // Daty
        editTextDataOdDialog.setText(aktualneKryteriaFiltrowania.getDataOd() != null ? aktualneKryteriaFiltrowania.getDataOd() : "");
        editTextDataDoDialog.setText(aktualneKryteriaFiltrowania.getDataDo() != null ? aktualneKryteriaFiltrowania.getDataDo() : "");
    }


    private void showDatePickerForDialog(final TextInputEditText editTextToSetDate) {
        Calendar newCalendar = Calendar.getInstance();
        // Spróbuj ustawić datę początkową na podstawie wartości w polu
        if (!TextUtils.isEmpty(editTextToSetDate.getText())) {
            try {
                newCalendar.setTime(dateFormatterUi.parse(editTextToSetDate.getText().toString()));
            } catch (Exception e) {
                Log.w(TAG, "Nie udało się sparsować daty z pola dialogu: " + editTextToSetDate.getText(), e);
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            editTextToSetDate.setText(dateFormatterUi.format(newDate.getTime()));
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


    private void fetchKontaForSpinner() {
        String token = tokenManager.getAuthToken();
        if (token == null) return;
        apiService.przeslijMojeKonta("Bearer " + token).enqueue(new Callback<List<KontoOdpowiedz>>() { // Załóżmy metodę pobierzMojeKonta
            @Override
            public void onResponse(Call<List<KontoOdpowiedz>> call, Response<List<KontoOdpowiedz>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaKontSpinner.clear();
                    listaKontSpinner.addAll(response.body());
                    // Adapter dla spinnera kont w dialogu zostanie zaktualizowany przy otwarciu dialogu
                } else {
                    Log.e(TAG, "Błąd pobierania kont dla spinnera: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<KontoOdpowiedz>> call, Throwable t) {
                Log.e(TAG, "Błąd sieci przy pobieraniu kont dla spinnera", t);
            }
        });
    }

    private void fetchKategorieForSpinner() {
        String token = tokenManager.getAuthToken();
        if (token == null) return;
        apiService.getKategorie("Bearer " + token).enqueue(new Callback<List<KategoriaOdpowiedz>>() { // Załóżmy metodę pobierzKategorie
            @Override
            public void onResponse(Call<List<KategoriaOdpowiedz>> call, Response<List<KategoriaOdpowiedz>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaKategoriiSpinner.clear();
                    listaKategoriiSpinner.addAll(response.body());
                    // Adapter dla spinnera kategorii w dialogu zostanie zaktualizowany przy otwarciu dialogu
                } else {
                    Log.e(TAG, "Błąd pobierania kategorii dla spinnera: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<KategoriaOdpowiedz>> call, Throwable t) {
                Log.e(TAG, "Błąd sieci przy pobieraniu kategorii dla spinnera", t);
            }
        });
    }


    private void pobierzIFiltrujTransakcje(TransakcjaPobieranieDTO kryteria) {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Brak autoryzacji.", Toast.LENGTH_SHORT).show();
            return;
        }
        //progressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "Wysyłanie kryteriów do API: " + kryteria.toString());

        apiService.pobierzTransakcjeDynamicznie("Bearer " + token, kryteria).enqueue(new Callback<List<TransakcjaOdpowiedz>>() {
            @Override
            public void onResponse(Call<List<TransakcjaOdpowiedz>> call, Response<List<TransakcjaOdpowiedz>> response) {
                // progressBar.setVisibility(View.GONE);
                listaTransakcji.clear();
                if (response.isSuccessful() && response.body() != null) {
                    listaTransakcji.addAll(response.body());
                    Log.d(TAG, "Pobrano transakcji po filtracji: " + listaTransakcji.size());
                    if (listaTransakcji.isEmpty()){
                        Toast.makeText(TransakcjeActivity.this, "Nie znaleziono transakcji dla podanych kryteriów.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Błąd filtrowania transakcji: " + response.code() + " " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            String errorMsg = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorMsg);
                            Toast.makeText(TransakcjeActivity.this, "Błąd: " + response.code() + " - " + errorMsg, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(TransakcjeActivity.this, "Błąd filtrowania transakcji: " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Błąd parsowania error body", e);
                    }
                }
                // transakcjeAdapter.notifyDataSetChanged(); // Odśwież RecyclerView
                // Tymczasowe odświeżenie
                if (recyclerViewTransactions.getAdapter() != null) {
                    recyclerViewTransactions.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<TransakcjaOdpowiedz>> call, Throwable t) {
                // progressBar.setVisibility(View.GONE);
                listaTransakcji.clear();
                // transakcjeAdapter.notifyDataSetChanged();
                if (recyclerViewTransactions.getAdapter() != null) {
                    recyclerViewTransactions.getAdapter().notifyDataSetChanged();
                }
                Log.e(TAG, "Błąd sieci przy filtrowaniu transakcji", t);
                Toast.makeText(TransakcjeActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateAktywneFiltryTextView() {
        StringBuilder filtryInfo = new StringBuilder("Aktywne filtry: ");
        boolean saFiltry = false;

        if (aktualneKryteriaFiltrowania.getTypTransakcji() != null) {
            filtryInfo.append("Typ: ").append(aktualneKryteriaFiltrowania.getTypTransakcji()).append("; ");
            saFiltry = true;
        }
        if (aktualneKryteriaFiltrowania.getKontoId() != null) {
            // Znajdź nazwę konta po ID dla lepszego UX
            KontoOdpowiedz konto = listaKontSpinner.stream()
                    .filter(k -> k.getId() == (aktualneKryteriaFiltrowania.getKontoId()))
                    .findFirst().orElse(null);
            if (konto != null) {
                filtryInfo.append("Konto: ").append(konto.getNazwa()).append("; ");
            } else if (!getIntent().hasExtra(EXTRA_KONTO_ID) || getIntent().getIntExtra(EXTRA_KONTO_ID, -1) != aktualneKryteriaFiltrowania.getKontoId()) {
                // Jeśli kontoId jest ustawione, ale nie znaleziono go na liście i nie jest to konto domyślne z intentu
                filtryInfo.append("Konto ID: ").append(aktualneKryteriaFiltrowania.getKontoId()).append("; ");
            }
            if (konto != null || (getIntent().hasExtra(EXTRA_KONTO_ID) && getIntent().getIntExtra(EXTRA_KONTO_ID, -1) == aktualneKryteriaFiltrowania.getKontoId() && listaKontSpinner.isEmpty())) {
                // Traktujemy to jako aktywny filtr jeśli znaleziono konto LUB jest to konto domyślne z intentu (nawet jeśli lista spinnera pusta)
                saFiltry = true;
            } else if (aktualneKryteriaFiltrowania.getKontoId() != null && (!getIntent().hasExtra(EXTRA_KONTO_ID) || getIntent().getIntExtra(EXTRA_KONTO_ID,-1) != aktualneKryteriaFiltrowania.getKontoId())){
                // Jeśli kontoId jest ustawione, ale nie jest to konto domyślne z intentu
                saFiltry = true;
            }
        }

        if (aktualneKryteriaFiltrowania.getKategoriaId() != null) {
            KategoriaOdpowiedz kategoria = listaKategoriiSpinner.stream()
                    .filter(k -> k.getId() == (aktualneKryteriaFiltrowania.getKategoriaId()))
                    .findFirst().orElse(null);
            if (kategoria != null) {
                filtryInfo.append("Kategoria: ").append(kategoria.getNazwa()).append("; ");
            } else {
                filtryInfo.append("Kategoria ID: ").append(aktualneKryteriaFiltrowania.getKategoriaId()).append("; ");
            }
            saFiltry = true;
        }
        if (aktualneKryteriaFiltrowania.getDataOd() != null) {
            filtryInfo.append("Od: ").append(aktualneKryteriaFiltrowania.getDataOd()).append("; ");
            saFiltry = true;
        }
        if (aktualneKryteriaFiltrowania.getDataDo() != null) {
            filtryInfo.append("Do: ").append(aktualneKryteriaFiltrowania.getDataDo()).append("; ");
            saFiltry = true;
        }

        if (saFiltry) {
            textViewAktywneFiltry.setText(filtryInfo.substring(0, filtryInfo.length() - 2)); // Usuń ostatni "; "
            textViewAktywneFiltry.setVisibility(View.VISIBLE);
        } else {
            textViewAktywneFiltry.setText("Aktywne filtry: Brak");
            textViewAktywneFiltry.setVisibility(View.GONE); // Lub zostaw widoczne z napisem "Brak"
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (tokenManager.hasToken()) {
            if (tokenManager.isSessionExpired()) {
                tokenManager.clearAuthToken();
                redirectToLogin(); // Twoja metoda przekierowująca do LoginActivity
                return;
            }
            tokenManager.updateLastActiveTime();
        } else {
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LogowanieActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

