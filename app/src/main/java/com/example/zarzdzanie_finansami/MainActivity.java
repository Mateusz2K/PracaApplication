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

import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.KontoWysylanie;
import com.example.zarzdzanie_finansami.dto.KontoOdpowiedz;
import com.example.zarzdzanie_finansami.dto.TransakcjaOdpowiedz;
import com.example.zarzdzanie_finansami.network.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;
import com.example.zarzdzanie_finansami.ui.DodajTransakcjeActivity; // Import nowej aktywności
import com.example.zarzdzanie_finansami.ui.LogowanieActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
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
    public static final int ADD_TRANSACTION_REQUEST_CODE = 1;


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
    private List<KontoOdpowiedz> listaKont = new ArrayList<>();
    private KontoOdpowiedz wybraneKonto;
    private List<TransakcjaOdpowiedz> transakcjeDlaKonta = new ArrayList<>();

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
        Intent intent = new Intent(MainActivity.this, LogowanieActivity.class);
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
                    wybraneKonto = null;
                    updateWidokDlaKonta();
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

        apiService.getMojeKonta("Bearer " + token).enqueue(new Callback<List<KontoOdpowiedz>>() {
            @Override
            public void onResponse(Call<List<KontoOdpowiedz>> call, Response<List<KontoOdpowiedz>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaKont.clear();
                    listaKont.addAll(response.body());
                    List<String> nazwyKont = listaKont.stream().map(KontoOdpowiedz::getNazwa).collect(Collectors.toList());
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerKonta.getAdapter();
                    adapter.clear();
                    if (nazwyKont.isEmpty()){
                        adapter.add("Brak kont. Dodaj nowe.");
                        buttonModifyKontoPopup.setEnabled(false);
                        buttonDodajTransakcjeDoKonta.setEnabled(false);
                        wybraneKonto = null; // Ustaw wybrane konto na null
                        updateWidokDlaKonta(); // Zaktualizuj widok
                    } else {
                        adapter.addAll(nazwyKont);
                        buttonModifyKontoPopup.setEnabled(true);
                        buttonDodajTransakcjeDoKonta.setEnabled(true);
                        if (!listaKont.isEmpty()) {
                            spinnerKonta.setSelection(0);
                            wybraneKonto = listaKont.get(0);
                            // updateWidokDlaKonta() zostanie wywołane przez onItemSelected listenera
                        }
                    }
                    adapter.notifyDataSetChanged();
                    // Jeżeli lista kont nie jest pusta, onItemSelected listener zadba o updateWidokDlaKonta
                    if (listaKont.isEmpty()) {
                        updateWidokDlaKonta(); // Jeśli lista jest pusta, musimy ręcznie zaktualizować widok
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Błąd pobierania kont: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<KontoOdpowiedz>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Błąd sieci (konta): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWidokDlaKonta() {
        if (wybraneKonto != null) {
            textViewAktualnyBilansKonta.setText("Aktualny bilans: " + currencyFormatter.format(wybraneKonto.getBilans()));
            pobierzTransakcjeDlaOkresu();
        } else {
            textViewAktualnyBilansKonta.setText("Wybierz konto lub dodaj nowe");
            textViewPrzychodyOkres.setText(currencyFormatter.format(0));
            textViewKosztyOkres.setText(currencyFormatter.format(0));
            textViewBilansOkres.setText("Bilans za okres: " + currencyFormatter.format(0));
            textViewBilansOkres.setTextColor(Color.BLACK);
            barChartFinanse.clear();
            barChartFinanse.invalidate();
            transakcjeDlaKonta.clear(); // Wyczyść listę transakcji
        }
    }

    private void setupChipGroupListener() {
        chipGroupOkres.setOnCheckedChangeListener((group, checkedId) -> {
            pobierzTransakcjeDlaOkresu();
        });
    }

    private void pobierzTransakcjeDlaOkresu() {
        if (wybraneKonto == null || !tokenManager.hasToken()) {
            transakcjeDlaKonta.clear();
            updatePodsumowanieOkresu();
            setupBarChart();
            return;
        }

        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();
        Date startDate;

        int checkedChipId = chipGroupOkres.getCheckedChipId();
        if (checkedChipId == R.id.chipDzien) {
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
            startDate = cal.getTime();
        } else if (checkedChipId == R.id.chipTydzien) {
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
            startDate = cal.getTime();
        } else if (checkedChipId == R.id.chipMiesiac) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
            startDate = cal.getTime();
        } else {
            // Domyślnie dzisiaj, jeśli nic nie jest zaznaczone (lub błąd)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
            startDate = cal.getTime();
            Log.w(TAG, "Nieznany Chip ID w chipGroupOkres, używam domyślnego 'Dziś'");
        }
        Log.d(TAG, "Wybrany okres: od " + dateFormatter.format(startDate) + " do " + dateFormatter.format(endDate));

        apiService.getTransakcjeDlaKonta("Bearer " + tokenManager.getAuthToken(), wybraneKonto.getId()).enqueue(new Callback<List<TransakcjaOdpowiedz>>() {
            @Override
            public void onResponse(Call<List<TransakcjaOdpowiedz>> call, Response<List<TransakcjaOdpowiedz>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final Date finalStartDate = startDate; // Potrzebne dla lambdy
                    transakcjeDlaKonta = response.body().stream()
                            .filter(t -> {
                                try {
                                    Date dataTransakcji = dateFormatter.parse(t.getData());
                                    Calendar endCal = Calendar.getInstance();
                                    endCal.setTime(endDate);
                                    endCal.set(Calendar.HOUR_OF_DAY, 23); endCal.set(Calendar.MINUTE, 59); endCal.set(Calendar.SECOND, 59);

                                    return !dataTransakcji.before(finalStartDate) && !dataTransakcji.after(endCal.getTime());
                                } catch (ParseException e) {
                                    Log.e(TAG, "Błąd parsowania daty transakcji: " + t.getData(), e);
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());
                    Log.d(TAG, "Przefiltrowano transakcji dla okresu: " + transakcjeDlaKonta.size());
                } else {
                    transakcjeDlaKonta.clear();
                    Toast.makeText(MainActivity.this, "Błąd pobierania transakcji dla okresu: " + response.code(), Toast.LENGTH_SHORT).show();
                }
                updatePodsumowanieOkresu();
                setupBarChart();
            }

            @Override
            public void onFailure(Call<List<TransakcjaOdpowiedz>> call, Throwable t) {
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

        for (TransakcjaOdpowiedz transakcja : transakcjeDlaKonta) {
            if (transakcja.getKwota() != null) {
                if ("PRZYCHOD".equalsIgnoreCase(transakcja.getTyp()) || "PRZYCHÓD".equalsIgnoreCase(transakcja.getTyp())) {
                    przychody = przychody.add(transakcja.getKwota());
                } else if ("KOSZT".equalsIgnoreCase(transakcja.getTyp())) {
                    koszty = koszty.add(transakcja.getKwota().abs());
                }
            }
        }

        textViewPrzychodyOkres.setText(currencyFormatter.format(przychody));
        textViewKosztyOkres.setText(currencyFormatter.format(koszty));

        BigDecimal bilans = przychody.subtract(koszty);
        textViewBilansOkres.setText("Bilans za okres: " + currencyFormatter.format(bilans));

        if (bilans.compareTo(BigDecimal.ZERO) >= 0) {
            textViewBilansOkres.setTextColor(ContextCompat.getColor(this, R.color.green_profit));
        } else {
            textViewBilansOkres.setTextColor(ContextCompat.getColor(this, R.color.red_loss));
        }
    }

    private void setupBarChart() {
        if (transakcjeDlaKonta == null || transakcjeDlaKonta.isEmpty()) {
            barChartFinanse.clear();
            barChartFinanse.invalidate();
            return;
        }

        BigDecimal przychody = BigDecimal.ZERO;
        BigDecimal koszty = BigDecimal.ZERO;

        for (TransakcjaOdpowiedz transakcja : transakcjeDlaKonta) {
            if (transakcja.getKwota() != null) {
                if ("PRZYCHOD".equalsIgnoreCase(transakcja.getTyp()) || "PRZYCHÓD".equalsIgnoreCase(transakcja.getTyp())) {
                    przychody = przychody.add(transakcja.getKwota());
                } else if ("KOSZT".equalsIgnoreCase(transakcja.getTyp())) {
                    koszty = koszty.add(transakcja.getKwota().abs());
                }
            }
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, przychody.floatValue()));
        entries.add(new BarEntry(1, koszty.floatValue()));

        BarDataSet dataSet = new BarDataSet(entries, "Finanse w okresie");
        dataSet.setColors(new int[]{ContextCompat.getColor(this, R.color.green_profit), ContextCompat.getColor(this, R.color.red_loss)});
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return currencyFormatter.format(value);
            }
        });


        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChartFinanse.setData(barData);
        barChartFinanse.getDescription().setEnabled(false);
        barChartFinanse.setDrawGridBackground(false);
        barChartFinanse.setFitBars(true);

        XAxis xAxis = barChartFinanse.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] labels = new String[]{"Przychody", "Koszty"};
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < labels.length) {
                    return labels[(int) value];
                }
                return "";
            }
        });

        barChartFinanse.getAxisLeft().setAxisMinimum(0f);
        barChartFinanse.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return currencyFormatter.format(value);
            }
        });
        barChartFinanse.getAxisRight().setEnabled(false);
        barChartFinanse.getLegend().setEnabled(false);

        barChartFinanse.animateY(1000);
        barChartFinanse.invalidate();
    }


    private void showModifyKontoDialog() {
        if (wybraneKonto == null) {
            Toast.makeText(this, "Najpierw wybierz konto", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_modify_konto, null);
        builder.setView(dialogView);

        EditText editTextNazwa = dialogView.findViewById(R.id.editTextModifyKontoNazwa);
        EditText editTextTyp = dialogView.findViewById(R.id.editTextModifyKontoTyp);
        EditText editTextBilans = dialogView.findViewById(R.id.editTextModifyKontoBilans); // Nowe pole

        editTextNazwa.setText(wybraneKonto.getNazwa());
        editTextTyp.setText(wybraneKonto.getTyp());
        // Możemy ustawić aktualny bilans jako placeholder lub pozostawić puste
        // editTextBilans.setText(wybraneKonto.getBilans().toString());


        builder.setTitle("Modyfikuj Konto")
                .setPositiveButton("Zapisz", (dialog, which) -> {
                    String nowaNazwa = editTextNazwa.getText().toString().trim();
                    String nowyTyp = editTextTyp.getText().toString().trim();
                    String nowyBilansStr = editTextBilans.getText().toString().trim();

                    if (TextUtils.isEmpty(nowaNazwa) || TextUtils.isEmpty(nowyTyp)) {
                        Toast.makeText(this, "Nazwa i typ konta nie mogą być puste.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    KontoWysylanie kontoWysylanie = new KontoWysylanie();
                    kontoWysylanie.setNazwa(nowaNazwa);
                    kontoWysylanie.setTyp(nowyTyp);
                    kontoWysylanie.setWaluta(wybraneKonto.getWaluta()); // Zachowaj oryginalną walutę

                    if (!TextUtils.isEmpty(nowyBilansStr)) {
                        try {
                            BigDecimal nowyBilans = new BigDecimal(nowyBilansStr);
                            kontoWysylanie.setBilans(nowyBilans);
                        } catch (NumberFormatException e) {
                            Toast.makeText(MainActivity.this, "Nieprawidłowy format bilansu.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        // Jeśli pole bilansu jest puste, nie wysyłamy go w requeście,
                        // backend powinien zignorować aktualizację bilansu.
                        // Alternatywnie, można wysłać oryginalny bilans, jeśli nie chcemy go zmieniać.
                        // kontoRequest.setBilans(wybraneKonto.getBilans());
                    }


                    updateKontoOnServer(wybraneKonto.getId(), kontoWysylanie);
                })
                .setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateKontoOnServer(int kontoId, KontoWysylanie kontoWysylanie) {
        String token = tokenManager.getAuthToken();
        if (token == null) return;

        apiService.updateKonto("Bearer " + token, kontoId, kontoWysylanie).enqueue(new Callback<KontoOdpowiedz>() {
            @Override
            public void onResponse(Call<KontoOdpowiedz> call, Response<KontoOdpowiedz> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this, "Konto zaktualizowane", Toast.LENGTH_SHORT).show();
                    fetchKonta(); // Odśwież listę kont w spinnerze i dane
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Brak szczegółów";
                        Log.e(TAG, "Błąd aktualizacji konta: " + response.code() + " - " + errorBody);
                        Toast.makeText(MainActivity.this, "Błąd aktualizacji konta: " + response.code() + "\n" + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Błąd parsowania error body", e);
                        Toast.makeText(MainActivity.this, "Błąd aktualizacji konta: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<KontoOdpowiedz> call, Throwable t) {
                Log.e(TAG, "Błąd sieci (aktualizacja konta): " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Błąd sieci (aktualizacja konta): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dodajTransakcje() {
        if (wybraneKonto == null) {
            Toast.makeText(this, "Wybierz konto, do którego chcesz dodać transakcję.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, DodajTransakcjeActivity.class);
        intent.putExtra(DodajTransakcjeActivity.EXTRA_KONTO_ID, wybraneKonto.getId());
        intent.putExtra(DodajTransakcjeActivity.EXTRA_KONTO_NAZWA, wybraneKonto.getNazwa());
        startActivityForResult(intent, ADD_TRANSACTION_REQUEST_CODE); // Użyj startActivityForResult
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TRANSACTION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Transakcja została dodana, odśwież dane
            Log.d(TAG, "Otrzymano wynik z AddTransactionActivity, odświeżam dane.");
            fetchKonta(); // Ponowne pobranie kont zaktualizuje bilans
            // pobierzTransakcjeDlaOkresu(); // Odświeży listę transakcji i wykres dla bieżącego okresu
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (tokenManager.hasToken()) {
            if (tokenManager.isSessionExpired()) {
                tokenManager.clearAuthToken();
                Toast.makeText(this, "Sesja wygasła.", Toast.LENGTH_SHORT).show();
                redirectToLogin(); // Twoja metoda przekierowująca do LoginActivity
                return;
            }
            tokenManager.updateLastActiveTime();
            fetchKonta();
        } else {
            redirectToLogin();
        }
    }
}