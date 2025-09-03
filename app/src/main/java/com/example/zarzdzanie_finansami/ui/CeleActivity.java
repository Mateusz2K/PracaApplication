package com.example.zarzdzanie_finansami.ui;

// Importy statyczne, jeśli są potrzebne (np. z DataKalendarzFragment)
import static com.example.zarzdzanie_finansami.ui.Adaptery.DataKalendarzFragment.REQUEST_KEY_DATE_PICKER;
import static com.example.zarzdzanie_finansami.ui.Adaptery.DataKalendarzFragment.RESULT_KEY_SELECTED_DATE_STRING;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.cel.CelOdpowiedz; // Zmieniono nazwę
import com.example.zarzdzanie_finansami.dto.cel.CelWysylanie; // Zmieniono nazwę
import com.example.zarzdzanie_finansami.dto.cel.ZasilenieCelu;  // Zmieniono nazwę
import com.example.zarzdzanie_finansami.dto.konto.KontoOdpowiedz;
import com.example.zarzdzanie_finansami.network.api.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;
import com.example.zarzdzanie_finansami.ui.Adaptery.CeleAdapter;
import com.example.zarzdzanie_finansami.ui.Adaptery.DataKalendarzFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.NumberFormat; // Do formatowania waluty
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CeleActivity extends AppCompatActivity implements CeleAdapter.OnCelInteractionListener {

    private static final String TAG = "CeleActivity";
    private static final String DIALOG_DATE_TAG_CELE = "DIALOG_DATE_PICKER_CELE";
    private static final String TYP_KONTA_OSZCZEDNOSCIOWE = "OSZCZĘDNOŚCIOWE";

    private Toolbar toolbar;
    private RecyclerView recyclerViewCele;
    private FloatingActionButton fabDodajCel;
    private CeleAdapter celeAdapter;
    private List<CelOdpowiedz> listaCelow = new ArrayList<>();
    private List<KontoOdpowiedz> listaWszystkichKont = new ArrayList<>();
    private List<KontoOdpowiedz> listaKontOszczednosciowych = new ArrayList<>();

    private ApiSerwis apiService;
    private TokenMenadzer tokenManager;
    private final DateTimeFormatter uiDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault());
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pl", "PL"));

    // Zmienna do przechowywania referencji do TextInputEditText, dla którego otwierany jest DatePicker
    private TextInputEditText targetEditTextForDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cele);

        toolbar = findViewById(R.id.toolbar_cele);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Moje Cele Oszczędnościowe");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewCele = findViewById(R.id.recyclerViewCele);
        fabDodajCel = findViewById(R.id.fabDodajCel);

        tokenManager = new TokenMenadzer(this);
        apiService = RetrofitKlient.getClient(this).create(ApiSerwis.class);

        setupRecyclerView();

        fabDodajCel.setOnClickListener(v -> {
            if (listaKontOszczednosciowych.isEmpty()) {
                Toast.makeText(this, "Musisz posiadać przynajmniej jedno konto oszczędnościowe, aby utworzyć nowy cel.", Toast.LENGTH_LONG).show();
            } else {
                showDialogDodajEdytujCel(null);
            }
        });

        if (tokenManager.hasToken()) {
            pobierzWszystkieKonta();
            pobierzCele();
        } else {
            Toast.makeText(this, "Sesja wygasła. Zaloguj się ponownie.", Toast.LENGTH_LONG).show();
            logoutUser();
        }

        setupDatePickerListener();
    }

    private void setupDatePickerListener() {
        // Nasłuchiwanie na wynik z DataKalendarzFragment
        getSupportFragmentManager().setFragmentResultListener(
                REQUEST_KEY_DATE_PICKER, // Używamy klucza z DataKalendarzFragment
                this,
                (requestKey, result) -> {
                    String selectedDateString = result.getString(RESULT_KEY_SELECTED_DATE_STRING);
                    if (targetEditTextForDate != null && selectedDateString != null) {
                        targetEditTextForDate.setText(selectedDateString);
                        targetEditTextForDate = null; // Zresetuj referencję po użyciu
                    }
                });
    }

    private void showDatePickerDialog(TextInputEditText targetEditText) {
        this.targetEditTextForDate = targetEditText; // Zapisz referencję
        DataKalendarzFragment datePicker = DataKalendarzFragment.newInstance();
        // Można dodać logikę do ustawienia daty początkowej w kalendarzu, jeśli targetEditText ma już wartość
        // np. parsując datę z targetEditText i przekazując do newInstance(year, month, day)
        datePicker.show(getSupportFragmentManager(), DIALOG_DATE_TAG_CELE);
    }


    private void setupRecyclerView() {
        celeAdapter = new CeleAdapter(this, listaCelow, this);
        recyclerViewCele.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCele.setAdapter(celeAdapter);
    }

    private void pobierzWszystkieKonta() {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Brak autoryzacji.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.przeslijMojeKonta("Bearer " + token).enqueue(new Callback<List<KontoOdpowiedz>>() {
            @Override
            public void onResponse(@NonNull Call<List<KontoOdpowiedz>> call, @NonNull Response<List<KontoOdpowiedz>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaWszystkichKont.clear();
                    listaWszystkichKont.addAll(response.body());

                    listaKontOszczednosciowych.clear();
                    for (KontoOdpowiedz konto : listaWszystkichKont) {
                        if (TYP_KONTA_OSZCZEDNOSCIOWE.equalsIgnoreCase(konto.getTyp())) {
                            listaKontOszczednosciowych.add(konto);
                        }
                    }
                    Log.d(TAG, "Pobrano kont: " + listaWszystkichKont.size() + ", oszczędnościowych: " + listaKontOszczednosciowych.size());
                } else {
                    handleApiError(response, "Błąd pobierania kont");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<KontoOdpowiedz>> call, @NonNull Throwable t) {
                handleNetworkError(t, "Błąd sieci (pobieranie kont)");
            }
        });
    }

    private void pobierzCele() {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Brak autoryzacji.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.pobierzCeleUzytkownika("Bearer " + token).enqueue(new Callback<List<CelOdpowiedz>>() {
            @Override
            public void onResponse(@NonNull Call<List<CelOdpowiedz>> call, @NonNull Response<List<CelOdpowiedz>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    celeAdapter.setCele(response.body()); // Adapter powinien obsłużyć czyszczenie i dodawanie
                    if (response.body().isEmpty()) {
                        Toast.makeText(CeleActivity.this, "Nie masz jeszcze żadnych celów.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    handleApiError(response, "Błąd pobierania celów");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CelOdpowiedz>> call, @NonNull Throwable t) {
                handleNetworkError(t, "Błąd sieci (pobieranie celów)");
            }
        });
    }

    private void showDialogDodajEdytujCel(@Nullable CelOdpowiedz celDoEdycji) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_dodaj_edytuj_cel, null);
        builder.setView(dialogView);

        TextInputEditText editTextNazwaCelu = dialogView.findViewById(R.id.editTextNazwaCelu);
        TextInputEditText editTextKwotaDocelowa = dialogView.findViewById(R.id.editTextKwotaDocelowaCelu);
        TextInputEditText editTextDataZakonczenia = dialogView.findViewById(R.id.editTextDataZakonczeniaCelu);
        TextInputEditText editTextOpisCelu = dialogView.findViewById(R.id.editTextOpisCelu);
        Spinner spinnerKontoDocelowe = dialogView.findViewById(R.id.spinnerKontoDoceloweCelu);

        editTextDataZakonczenia.setFocusable(false);
        editTextDataZakonczenia.setClickable(true);
        editTextDataZakonczenia.setOnClickListener(v -> showDatePickerDialog(editTextDataZakonczenia));

        if (listaKontOszczednosciowych.isEmpty() && celDoEdycji == null) {
            // Toast już jest w FAB onClickListener
            // Można tu np. dialog.dismiss() jeśli jest już pokazany, ale FAB powinien zapobiec temu.
        }

        List<String> nazwyKontOszczednosciowych = listaKontOszczednosciowych.stream()
                .map(KontoOdpowiedz::getNazwa)
                .collect(Collectors.toList());
        ArrayAdapter<String> adapterKont = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                nazwyKontOszczednosciowych);
        adapterKont.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKontoDocelowe.setAdapter(adapterKont);

        if (celDoEdycji != null) {
            builder.setTitle("Edytuj Cel");
            editTextNazwaCelu.setText(celDoEdycji.getNazwaCelu());
            editTextKwotaDocelowa.setText(celDoEdycji.getKwotaDocelowa().toString());
            if (celDoEdycji.getDataZakonczenia() != null) {
                editTextDataZakonczenia.setText(celDoEdycji.getDataZakonczenia().format(uiDateFormatter));
            }
            editTextOpisCelu.setText(celDoEdycji.getOpis());

            int kontoIdCelu = celDoEdycji.getKontoId();
            for (int i = 0; i < listaKontOszczednosciowych.size(); i++) {
                if (listaKontOszczednosciowych.get(i).getId() == kontoIdCelu) {
                    spinnerKontoDocelowe.setSelection(i);
                    break;
                }
            }
            spinnerKontoDocelowe.setEnabled(false);
        } else {
            builder.setTitle("Dodaj Nowy Cel");
            spinnerKontoDocelowe.setEnabled(!listaKontOszczednosciowych.isEmpty());
            spinnerKontoDocelowe.setVisibility(listaKontOszczednosciowych.isEmpty() ? View.GONE : View.VISIBLE);
        }

        builder.setPositiveButton(celDoEdycji != null ? "Zapisz" : "Dodaj", null);
        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button buttonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            buttonPositive.setOnClickListener(view -> {
                String nazwa = editTextNazwaCelu.getText().toString().trim();
                String kwotaStr = editTextKwotaDocelowa.getText().toString().trim();
                String dataZakStr = editTextDataZakonczenia.getText().toString().trim();
                String opis = editTextOpisCelu.getText().toString().trim();

                if (TextUtils.isEmpty(nazwa)) {
                    editTextNazwaCelu.setError("Nazwa celu jest wymagana.");
                    editTextNazwaCelu.requestFocus();
                    return;
                }
                editTextNazwaCelu.setError(null); // Clear error

                if (TextUtils.isEmpty(kwotaStr)) {
                    editTextKwotaDocelowa.setError("Kwota docelowa jest wymagana.");
                    editTextKwotaDocelowa.requestFocus();
                    return;
                }
                editTextKwotaDocelowa.setError(null);

                BigDecimal kwotaDocelowa;
                try {
                    kwotaDocelowa = new BigDecimal(kwotaStr);
                    if (kwotaDocelowa.compareTo(BigDecimal.ZERO) <= 0) {
                        editTextKwotaDocelowa.setError("Kwota docelowa musi być dodatnia.");
                        editTextKwotaDocelowa.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    editTextKwotaDocelowa.setError("Nieprawidłowy format kwoty.");
                    editTextKwotaDocelowa.requestFocus();
                    return;
                }
                editTextKwotaDocelowa.setError(null);


                LocalDate dataZakonczenia = null;
                if (!TextUtils.isEmpty(dataZakStr)) {
                    try {
                        dataZakonczenia = LocalDate.parse(dataZakStr, uiDateFormatter);
                    } catch (DateTimeParseException e) {
                        editTextDataZakonczenia.setError("Format daty: dd.MM.yyyy");
                        editTextDataZakonczenia.requestFocus();
                        return;
                    }
                }
                editTextDataZakonczenia.setError(null);


                Integer wybraneKontoId = null;
                if (spinnerKontoDocelowe.getVisibility() == View.VISIBLE &&
                        spinnerKontoDocelowe.isEnabled() &&
                        spinnerKontoDocelowe.getSelectedItemPosition() != Spinner.INVALID_POSITION) {
                    if (!listaKontOszczednosciowych.isEmpty()) {
                        wybraneKontoId = listaKontOszczednosciowych.get(spinnerKontoDocelowe.getSelectedItemPosition()).getId();
                    }
                }

                if (celDoEdycji == null && wybraneKontoId == null) {
                    Toast.makeText(CeleActivity.this, "Wybierz konto oszczędnościowe dla celu.", Toast.LENGTH_SHORT).show();
                    return;
                }

                CelWysylanie dto = new CelWysylanie();
                dto.setNazwa(nazwa); // Poprawiona nazwa settera
                dto.setKwotaDocelowa(kwotaDocelowa);
                if (dataZakonczenia != null) {
                    dto.setDataZakonczenia(dataZakonczenia.format(uiDateFormatter)); // Formatuj LocalDate na String
                } else {
                    dto.setDataZakonczenia(null); // Lub pusty string, jeśli API tego oczekuje dla braku daty
                }                dto.setOpis(opis);

                if (celDoEdycji != null) {
                    dto.setKontoId(celDoEdycji.getKontoId());
                    aktualizujCelNaSerwerze(celDoEdycji.getId(), dto, dialog);
                } else {
                    dto.setKontoId(wybraneKontoId);
                    dodajCelNaSerwerze(dto, dialog);
                }
            });
        });
        dialog.show();
    }


    private void dodajCelNaSerwerze(CelWysylanie dto, AlertDialog dialogToDismiss) {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Brak autoryzacji.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.stworzCel("Bearer " + token, dto).enqueue(new Callback<CelOdpowiedz>() {
            @Override
            public void onResponse(@NonNull Call<CelOdpowiedz> call, @NonNull Response<CelOdpowiedz> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CeleActivity.this, "Cel \"" + response.body().getNazwaCelu() + "\" dodany!", Toast.LENGTH_SHORT).show();
                    pobierzCele();
                    if (dialogToDismiss != null) dialogToDismiss.dismiss();
                } else {
                    handleApiError(response, "Błąd dodawania celu");
                }
            }

            @Override
            public void onFailure(@NonNull Call<CelOdpowiedz> call, @NonNull Throwable t) {
                handleNetworkError(t, "Błąd sieci (dodawanie celu)");
            }
        });
    }

    private void aktualizujCelNaSerwerze(int celId, CelWysylanie dto, AlertDialog dialogToDismiss) {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Brak autoryzacji.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.aktualizujCel("Bearer " + token, celId, dto).enqueue(new Callback<CelOdpowiedz>() {
            @Override
            public void onResponse(@NonNull Call<CelOdpowiedz> call, @NonNull Response<CelOdpowiedz> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CeleActivity.this, "Cel \"" + response.body().getNazwaCelu() + "\" zaktualizowany!", Toast.LENGTH_SHORT).show();
                    pobierzCele();
                    if (dialogToDismiss != null) dialogToDismiss.dismiss();
                } else {
                    handleApiError(response, "Błąd aktualizacji celu");
                }
            }

            @Override
            public void onFailure(@NonNull Call<CelOdpowiedz> call, @NonNull Throwable t) {
                handleNetworkError(t, "Błąd sieci (aktualizacja celu)");
            }
        });
    }


    @Override
    public void onEdytujCelClicked(CelOdpowiedz cel, int position) {
        showDialogDodajEdytujCel(cel);
    }

    @Override
    public void onUsunCelClicked(CelOdpowiedz cel, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Usuń Cel")
                .setMessage("Czy na pewno chcesz usunąć cel \"" + cel.getNazwaCelu() + "\"?")
                .setPositiveButton("Usuń", (dialog, which) -> usunCelZSerwera(cel.getId()))
                .setNegativeButton("Anuluj", null)
                .setIcon(android.R.drawable.ic_dialog_alert) // Dobrze jest dodać ikonę
                .show();
    }

    private void usunCelZSerwera(int celId) {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Brak autoryzacji.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.usunCel("Bearer " + token, celId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CeleActivity.this, "Cel usunięty", Toast.LENGTH_SHORT).show();
                    pobierzCele();
                } else {
                    handleApiError(response, "Błąd usuwania celu");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                handleNetworkError(t, "Błąd sieci (usuwanie celu)");
            }
        });
    }

    @Override
    public void onDodajSrodkiClicked(CelOdpowiedz cel, int position) {
        if (cel.getStatus() == CelStatusEnumAndroid.ZAKOŃCZONY) {
            Toast.makeText(this, "Cel \"" + cel.getNazwaCelu() + "\" jest już zakończony.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dodaj środki do: " + cel.getNazwaCelu());

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_dodaj_srodki_do_celu, null);
        builder.setView(dialogView);

        // Zalecane użycie TextInputEditText w TextInputLayout dla lepszej obsługi błędów
        EditText editTextKwotaDoDodania = dialogView.findViewById(R.id.editTextKwotaDoDodaniaCel);
        Spinner spinnerKontoZrodlowe = dialogView.findViewById(R.id.spinnerKontoZrodloweCel);

        List<String> opcjeKontaZrodlowego = new ArrayList<>();
        opcjeKontaZrodlowego.add("Wpłata z zewnątrz"); // Indeks 0

        // Filtrujemy konta, aby nie można było wybrać konta celu jako źródłowego
        List<KontoOdpowiedz> dostepneKontaZrodlowe = listaWszystkichKont.stream()
                .filter(k -> k.getId() != cel.getKontoId())
                .collect(Collectors.toList());

        dostepneKontaZrodlowe.forEach(k -> opcjeKontaZrodlowego.add(k.getNazwa() + " (" + currencyFormatter.format(k.getBilans()) + ")"));


        ArrayAdapter<String> adapterKontZrodlowych = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opcjeKontaZrodlowego);
        adapterKontZrodlowych.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKontoZrodlowe.setAdapter(adapterKontZrodlowych);

        builder.setPositiveButton("Dodaj", null);
        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button buttonPositive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            buttonPositive.setOnClickListener(v -> {
                String kwotaStr = editTextKwotaDoDodania.getText().toString().trim();
                if (TextUtils.isEmpty(kwotaStr)) {
                    Toast.makeText(this, "Kwota jest wymagana.", Toast.LENGTH_SHORT).show();
                    return;
                }

                BigDecimal kwotaDoDodania;
                try {
                    kwotaDoDodania = new BigDecimal(kwotaStr);
                    if (kwotaDoDodania.compareTo(BigDecimal.ZERO) <= 0) {
                        Toast.makeText(this, "Kwota musi być dodatnia.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Nieprawidłowy format kwoty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Integer wybraneKontoZrodloweId = null;
                int selectedPosition = spinnerKontoZrodlowe.getSelectedItemPosition();

                if (selectedPosition > 0) { // Pozycja 0 to "Wpłata z zewnątrz"
                    // Indeks w `dostepneKontaZrodlowe` to `selectedPosition - 1`
                    if ((selectedPosition - 1) < dostepneKontaZrodlowe.size()) {
                        KontoOdpowiedz wybraneKonto = dostepneKontaZrodlowe.get(selectedPosition - 1);
                        wybraneKontoZrodloweId = wybraneKonto.getId();

                        // Walidacja salda (już jest na backendzie, ale można dodać też tu)
                        if (wybraneKonto.getBilans().compareTo(kwotaDoDodania) < 0) {
                            Toast.makeText(this, "Niewystarczające środki na wybranym koncie: " + wybraneKonto.getNazwa(), Toast.LENGTH_LONG).show();
                            return;
                        }
                    } else {
                        Toast.makeText(this, "Błąd wyboru konta źródłowego.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                ZasilenieCelu zasilenieDTO = new ZasilenieCelu(kwotaDoDodania, wybraneKontoZrodloweId);
                dodajSrodkiDoCeluNaSerwerze(cel.getId(), zasilenieDTO, dialog);
            });
        });
        dialog.show();
    }

    private void dodajSrodkiDoCeluNaSerwerze(int celId, ZasilenieCelu zasilenieDTO, AlertDialog dialogToDismiss) {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Brak autoryzacji.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.dodajSrodkiDoCelu("Bearer " + token, celId, zasilenieDTO).enqueue(new Callback<CelOdpowiedz>() {
            @Override
            public void onResponse(@NonNull Call<CelOdpowiedz> call, @NonNull Response<CelOdpowiedz> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CeleActivity.this, "Środki dodane do celu!", Toast.LENGTH_SHORT).show();
                    pobierzCele();
                    if (zasilenieDTO.getKontoZrodloweId() != null) {
                        pobierzWszystkieKonta();
                    }
                    if (dialogToDismiss != null) dialogToDismiss.dismiss();
                } else {
                    handleApiError(response, "Błąd dodawania środków");
                }
            }

            @Override
            public void onFailure(@NonNull Call<CelOdpowiedz> call, @NonNull Throwable t) {
                handleNetworkError(t, "Błąd sieci (dodawanie środków)");
            }
        });
    }

    private void handleApiError(Response<?> response, String defaultMessagePrefix) {
        String errorMsg = defaultMessagePrefix;
        int errorCode = response.code();
        if (response.errorBody() != null) {
            try {
                String errorBodyStr = response.errorBody().string();
                // TODO: Rozważ bardziej zaawansowane parsowanie JSONa błędu, jeśli backend zwraca strukturalne błędy
                errorMsg = defaultMessagePrefix + " (" + errorCode + "): " + errorBodyStr;
            } catch (Exception e) {
                Log.e(TAG, "Error parsing errorBody", e);
                errorMsg = defaultMessagePrefix + " (kod: " + errorCode + ")";
            }
        } else {
            errorMsg = defaultMessagePrefix + " (kod: " + errorCode + ")";
        }
        Toast.makeText(CeleActivity.this, errorMsg, Toast.LENGTH_LONG).show();
        Log.e(TAG, errorMsg);
    }

    private void handleNetworkError(Throwable t, String defaultMessagePrefix) {
        Log.e(TAG, defaultMessagePrefix + ": " + t.getMessage(), t);
        Toast.makeText(CeleActivity.this, defaultMessagePrefix + ". Sprawdź połączenie.", Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void logoutUser() {
        tokenManager.clearAuthToken();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LogowanieActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
            pobierzCele();
        } else {
            redirectToLogin();
        }
    }
}
