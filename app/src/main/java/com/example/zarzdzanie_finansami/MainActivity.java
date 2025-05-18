package com.example.zarzdzanie_finansami;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.KontoRequest;
import com.example.zarzdzanie_finansami.dto.KontoResponse;
import com.example.zarzdzanie_finansami.dto.TransakcjaResponse; // Będziesz potrzebował tego DTO
import com.example.zarzdzanie_finansami.network.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;
import com.example.zarzdzanie_finansami.ui.LoginActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter; // Zamiast IndexAxisValueFormatter dla nowszych wersji
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private Spinner spinnerKonta;
    private ImageButton buttonModifyKontoPopup;
    private TextView textViewAktualnyBilansKonta;
    private ChipGroup chipGroupOkres;
    private Chip chipDzien, chipTydzien, chipMiesiac;
    private TextView textViewPrzychodyOkres, textViewKosztyOkres, textViewBilansOkres;
    private BarChart barChartFinanse;
    private Button buttonDodajTransakcjeDoKonta;

    private ApiSerwis apiService;
    private TokenMenadzer tokenManager;
    private List<KontoResponse> listaKont = new ArrayList<>();
    private KontoResponse wybraneKonto;
    private List<TransakcjaResponse> transakcjeDlaKonta = new ArrayList<>(); // Lista transakcji dla wybranego konta i okresu

    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_dashboard);
        setSupportActionBar(toolbar);

        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pl", "PL"));

        spinnerKonta = findViewById(R.id.spinnerKonta);
        buttonModifyKontoPopup = findViewById(R.id.buttonModifyKontoPopup);
        textViewAktualnyBilansKonta = findViewById(R.id.textViewAktualnyBilansKonta);
        chipGroupOkres = findViewById(R.id.chipGroupOkres);
        chipDzien = findViewById(R.id.chipDzien);
        chipTydzien = findViewById(R.id.chipTydzien);
        chipMiesiac = findViewById(R.id.chipMiesiac);
        textViewPrzychodyOkres = findViewById(R.id.textViewPrzychodyOkres);
        textViewKosztyOkres = findViewById(R.id.textViewKosztyOkres);
        textViewBilansOkres = findViewById(R.id.textViewBilansOkres);
        barChartFinanse = findViewById(R.id.barChartFinanse);
        buttonDodajTransakcjeDoKonta = findViewById(R.id.buttonDodajTransakcjeDoKonta);

        tokenManager = new TokenMenadzer(this);
        apiService = RetrofitKlient.getClient().create(ApiSerwis.class);

        if (!tokenManager.hasToken()) {
            redirectToLogin();
            return;
        }

        setupSpinnerKonta();
        setupChipGroupListener();
        buttonModifyKontoPopup.setOnClickListener(v -> showModifyKontoDialog());
        buttonDodajTransakcjeDoKonta.setOnClickListener(v -> dodajTransakcje());

        fetchKonta();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupSpinnerKonta() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKonta.setAdapter(adapter);

        spinnerKonta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < listaKont.size()) {
                    wybraneKonto = listaKont.get(position);
                    Log.d(TAG, "Wybrano konto: " + wybraneKonto.getNazwa());
                    updateWidokDlaKonta();
                } else if (listaKont.isEmpty() && position == 0 && parent.getItemAtPosition(position) != null) {
                    // Obsługa przypadku, gdy jest tylko placeholder "Wybierz konto" lub "Brak kont"
                    wybraneKonto = null;
                    updateWidokDlaKonta(); // Czyści widok lub pokazuje stan "brak konta"
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                wybraneKonto = null;
                updateWidokDlaKonta();
            }
        });
    }

    private void fetchKonta() {
        String token = tokenManager.getAuthToken();
        if (token == null) return;

        apiService.getMojeKonta("Bearer " + token).enqueue(new Callback<List<KontoResponse>>() {
            @Override
            public void onResponse(Call<List<KontoResponse>> call, Response<List<KontoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaKont.clear();
                    listaKont.addAll(response.body());
                    List<String> nazwyKont = listaKont.stream().map(KontoResponse::getNazwa).collect(Collectors.toList());
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerKonta.getAdapter();
                    adapter.clear();
                    if (nazwyKont.isEmpty()){
                        adapter.add("Brak kont. Dodaj nowe.");
                        buttonModifyKontoPopup.setEnabled(false);
                        buttonDodajTransakcjeDoKonta.setEnabled(false);
                    } else {
                        adapter.addAll(nazwyKont);
                        buttonModifyKontoPopup.setEnabled(true);
                        buttonDodajTransakcjeDoKonta.setEnabled(true);
                        if (!listaKont.isEmpty()) {
                            spinnerKonta.setSelection(0); // Automatycznie wybierz pierwsze konto
                            wybraneKonto = listaKont.get(0);
                            updateWidokDlaKonta();
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Błąd pobierania kont: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<KontoResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Błąd sieci (konta): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWidokDlaKonta() {
        if (wybraneKonto != null) {
            textViewAktualnyBilansKonta.setText("Aktualny bilans: " + currencyFormatter.format(wybraneKonto.getBilans()));
            // Po wybraniu konta, pobierz transakcje dla domyślnego okresu (np. Dziś)
            pobierzTransakcjeDlaOkresu();
        } else {
            textViewAktualnyBilansKonta.setText("Wybierz konto");
            textViewPrzychodyOkres.setText(currencyFormatter.format(0));
            textViewKosztyOkres.setText(currencyFormatter.format(0));
            textViewBilansOkres.setText("Bilans za okres: " + currencyFormatter.format(0));
            textViewBilansOkres.setTextColor(Color.BLACK); // Domyślny kolor
            barChartFinanse.clear();
            barChartFinanse.invalidate(); // Odśwież wykres
        }
    }

    private void setupChipGroupListener() {
        chipGroupOkres.setOnCheckedChangeListener((group, checkedId) -> {
            pobierzTransakcjeDlaOkresu();
        });
    }

    private void pobierzTransakcjeDlaOkresu() {
        if (wybraneKonto == null || !tokenManager.hasToken()) {
            // Wyczyść poprzednie dane jeśli nie ma wybranego konta
            transakcjeDlaKonta.clear();
            updatePodsumowanieOkresu();
            setupBarChart();
            return;
        }

        // Tutaj powinna być logika pobierania transakcji z API dla wybranego konta i okresu
        // Na potrzeby przykładu, zakładam, że masz metodę w ApiSerwis:
        // Call<List<TransakcjaResponse>> getTransakcjeKontaWgOkresu(String token, int kontoId, String startDate, String endDate);

        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime(); // Koniec to teraz
        Date startDate;

        int checkedChipId = chipGroupOkres.getCheckedChipId();
        if (checkedChipId == R.id.chipDzien) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startDate = cal.getTime();
        } else if (checkedChipId == R.id.chipTydzien) {
            cal.add(Calendar.DAY_OF_WEEK, - (cal.get(Calendar.DAY_OF_WEEK) - cal.getFirstDayOfWeek()));
            cal.set(Calendar.HOUR_OF_DAY, 0); // Początek dnia
            startDate = cal.getTime();
        } else if (checkedChipId == R.id.chipMiesiac) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0); // Początek dnia
            startDate = cal.getTime();
        } else {
            // Domyślnie np. dzisiaj lub brak - wyczyść dane
            transakcjeDlaKonta.clear();
            updatePodsumowanieOkresu();
            setupBarChart();
            return;
        }
        Log.d(TAG, "Wybrany okres: od " + dateFormatter.format(startDate) + " do " + dateFormatter.format(endDate));

        // Przykład wywołania API (musisz dodać odpowiednią metodę do ApiSerwis.java)
        // W rzeczywistości backend powinien sam agregować dane dla okresów.
        // Pobieranie wszystkich transakcji i filtrowanie po stronie klienta jest nieefektywne dla dużych zbiorów.
        // Na razie zasymulujemy, że pobieramy wszystkie i filtrujemy.
        // Idealnie: apiService.getAggregatedDataForPeriod(token, wybraneKonto.getId(), odData, doData)
        apiService.getTransakcjeDlaKonta("Bearer " + tokenManager.getAuthToken(), wybraneKonto.getId()).enqueue(new Callback<List<TransakcjaResponse>>() {
            @Override
            public void onResponse(Call<List<TransakcjaResponse>> call, Response<List<TransakcjaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Filtrowanie po stronie klienta (tymczasowe, do demonstracji)
                    transakcjeDlaKonta = response.body().stream()
                            .filter(t -> {
                                try {
                                    Date dataTransakcji = dateFormatter.parse(t.getData()); // Założenie: getData() zwraca String yyyy-MM-dd
                                    // Sprawdź, czy dataTransakcji jest po startDate i przed (lub równa) endDate.
                                    // Trzeba być ostrożnym z porównywaniem dat, upewnij się, że endDate obejmuje cały dzień.
                                    Calendar endCal = Calendar.getInstance();
                                    endCal.setTime(endDate);
                                    endCal.set(Calendar.HOUR_OF_DAY, 23);
                                    endCal.set(Calendar.MINUTE, 59);
                                    endCal.set(Calendar.SECOND, 59);

                                    return !dataTransakcji.before(startDate) && !dataTransakcji.after(endCal.getTime());
                                } catch (Exception e) {
                                    Log.e(TAG, "Błąd parsowania daty transakcji: " + t.getData(), e);
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());
                    Log.d(TAG, "Przefiltrowano transakcji dla okresu: " + transakcjeDlaKonta.size());
                } else {
                    transakcjeDlaKonta.clear();
                    Toast.makeText(MainActivity.this, "Błąd pobierania transakcji dla okresu", Toast.LENGTH_SHORT).show();
                }
                updatePodsumowanieOkresu();
                setupBarChart();
            }

            @Override
            public void onFailure(Call<List<TransakcjaResponse>> call, Throwable t) {
                transakcjeDlaKonta.clear();
                updatePodsumowanieOkresu();
                setupBarChart();
                Toast.makeText(MainActivity.this, "Błąd sieci (transakcje): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePodsumowanieOkresu() {
        BigDecimal przychody = BigDecimal.ZERO;
        BigDecimal koszty = BigDecimal.ZERO;

        for (TransakcjaResponse transakcja : transakcjeDlaKonta) {
            if (transakcja.getKwota() != null) {
                // Założenie: "PRZYCHÓD" lub "WYDATEK" w transakcja.getTyp()
                if ("PRZYCHOD".equalsIgnoreCase(transakcja.getTyp()) || "PRZYCHÓD".equalsIgnoreCase(transakcja.getTyp())) {
                    przychody = przychody.add(transakcja.getKwota());
                } else if ("WYDATEK".equalsIgnoreCase(transakcja.getTyp())) {
                    koszty = koszty.add(transakcja.getKwota().abs()); // Koszty jako wartość dodatnia
                }
            }
        }

        textViewPrzychodyOkres.setText(currencyFormatter.format(przychody));
        textViewKosztyOkres.setText(currencyFormatter.format(koszty));

        BigDecimal bilans = przychody.subtract(koszty);
        textViewBilansOkres.setText("Bilans za okres: " + currencyFormatter.format(bilans));

        if (bilans.compareTo(BigDecimal.ZERO) >= 0) {
            textViewBilansOkres.setTextColor(ContextCompat.getColor(this, R.color.green_profit)); // Definiuj w colors.xml
        } else {
            textViewBilansOkres.setTextColor(ContextCompat.getColor(this, R.color.red_loss)); // Definiuj w colors.xml
        }
    }

    private void setupBarChart() {
        if (transakcjeDlaKonta == null || transakcjeDlaKonta.isEmpty()) {
            barChartFinanse.clear();
            barChartFinanse.invalidate(); // Odśwież wykres, aby pokazać, że jest pusty
            return;
        }

        BigDecimal przychody = BigDecimal.ZERO;
        BigDecimal koszty = BigDecimal.ZERO;

        for (TransakcjaResponse transakcja : transakcjeDlaKonta) {
            if (transakcja.getKwota() != null) {
                if ("PRZYCHOD".equalsIgnoreCase(transakcja.getTyp()) || "PRZYCHÓD".equalsIgnoreCase(transakcja.getTyp())) { //
                    przychody = przychody.add(transakcja.getKwota()); //
                } else if ("WYDATEK".equalsIgnoreCase(transakcja.getTyp())) { //
                    koszty = koszty.add(transakcja.getKwota().abs()); //
                }
            }
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, przychody.floatValue())); // Przychody
        entries.add(new BarEntry(1, koszty.floatValue()));   // Koszty

        BarDataSet dataSet = new BarDataSet(entries, "Finanse w okresie");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Kolory słupków
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f); // Szerokość słupków

        barChartFinanse.setData(barData);
        barChartFinanse.getDescription().setEnabled(false); // Wyłącz opis wykresu
        barChartFinanse.setDrawGridBackground(false);
        barChartFinanse.setFitBars(true); // Dopasuj słupki do szerokości

        XAxis xAxis = barChartFinanse.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // Odstęp między etykietami osi X
        xAxis.setValueFormatter(new ValueFormatter() { // Zamiast IndexAxisValueFormatter
            private final String[] labels = new String[]{"Przychody", "Koszty"};
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < labels.length) {
                    return labels[(int) value];
                }
                return "";
            }
        });


        barChartFinanse.getAxisLeft().setAxisMinimum(0f); // Minimalna wartość osi Y
        barChartFinanse.getAxisRight().setEnabled(false); // Wyłącz prawą oś Y
        barChartFinanse.getLegend().setEnabled(false); // Wyłącz legendę

        barChartFinanse.animateY(1000); // Animacja
        barChartFinanse.invalidate(); // Odśwież wykres
    }


    private void showModifyKontoDialog() {
        if (wybraneKonto == null) {
            Toast.makeText(this, "Najpierw wybierz konto", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        // Możesz użyć tego samego layoutu co w KontaActivity lub stworzyć nowy
        View dialogView = inflater.inflate(R.layout.dialog_modify_konto, null);
        builder.setView(dialogView);

        EditText editTextNazwa = dialogView.findViewById(R.id.editTextModifyKontoNazwa);
        EditText editTextTyp = dialogView.findViewById(R.id.editTextModifyKontoTyp);

        editTextNazwa.setText(wybraneKonto.getNazwa());
        editTextTyp.setText(wybraneKonto.getTyp());

        builder.setTitle("Modyfikuj Konto")
                .setPositiveButton("Zapisz", (dialog, which) -> {
                    String nowaNazwa = editTextNazwa.getText().toString().trim();
                    String nowyTyp = editTextTyp.getText().toString().trim();

                    if (TextUtils.isEmpty(nowaNazwa) || TextUtils.isEmpty(nowyTyp)) {
                        Toast.makeText(this, "Nazwa i typ konta nie mogą być puste.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    KontoRequest kontoRequest = new KontoRequest();
                    kontoRequest.setNazwa(nowaNazwa);
                    kontoRequest.setTyp(nowyTyp);
                    // Ustaw inne pola, jeśli serwer na to pozwala (np. waluta)
                    // kontoRequest.setWaluta(wybraneKonto.getWaluta());

                    updateKontoOnServer(wybraneKonto.getId(), kontoRequest);
                })
                .setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateKontoOnServer(int kontoId, KontoRequest kontoRequest) {
        String token = tokenManager.getAuthToken();
        if (token == null) return;

        apiService.updateKonto("Bearer " + token, kontoId, kontoRequest).enqueue(new Callback<KontoResponse>() {
            @Override
            public void onResponse(Call<KontoResponse> call, Response<KontoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this, "Konto zaktualizowane", Toast.LENGTH_SHORT).show();
                    fetchKonta(); // Odśwież listę kont w spinnerze i dane
                } else {
                    Toast.makeText(MainActivity.this, "Błąd aktualizacji konta: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<KontoResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Błąd sieci (aktualizacja konta): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dodajTransakcje() {
        if (wybraneKonto == null) {
            Toast.makeText(this, "Wybierz konto, do którego chcesz dodać transakcję.", Toast.LENGTH_LONG).show();
            return;
        }
        // TODO: Otwórz nową aktywność/dialog do dodawania transakcji, przekazując ID wybranego konta
        // Intent intent = new Intent(this, AddTransactionActivity.class);
        // intent.putExtra(AddTransactionActivity.EXTRA_KONTO_ID, wybraneKonto.getId());
        // startActivity(intent);
        Toast.makeText(this, "Przejście do dodawania transakcji dla konta: " + wybraneKonto.getNazwa(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Odśwież dane po powrocie do aktywności
        if (tokenManager.hasToken()) {
            fetchKonta(); // To zainicjuje też pobranie transakcji dla wybranego konta i okresu
        } else {
            redirectToLogin();
        }
    }
}