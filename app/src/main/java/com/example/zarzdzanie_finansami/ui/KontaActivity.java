package com.example.zarzdzanie_finansami.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.zarzdzanie_finansami.ui.TransakcjeActivity;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.konto.KontoWysylanie; // Potrzebne DTO do wysyłania danych
import com.example.zarzdzanie_finansami.dto.konto.KontoOdpowiedz;
import com.example.zarzdzanie_finansami.dto.konto.TypKontaEnum;
import com.example.zarzdzanie_finansami.network.api.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;
import com.example.zarzdzanie_finansami.ui.Adaptery.KontaAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KontaActivity extends AppCompatActivity implements KontaAdapter.OnKontoInteractionListener {

    private static final String TAG = "KontaActivity";
    private RecyclerView recyclerViewKonta;
    private Button buttonLogout;
    private FloatingActionButton fabAddKonto;
    private ApiSerwis apiService;
    private TokenMenadzer tokenManager;
    private KontaAdapter kontaAdapter;
    private List<KontoOdpowiedz> kontoList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konta);

        Toolbar toolbar = findViewById(R.id.toolbar_konta);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Moje Konta");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }

        recyclerViewKonta = findViewById(R.id.recyclerViewKonta);
        buttonLogout = findViewById(R.id.buttonLogoutKonta);
        fabAddKonto = findViewById(R.id.fabAddKonto);

        tokenManager = new TokenMenadzer(this);
        apiService = RetrofitKlient.getClient(this).create(ApiSerwis.class);

        setupRecyclerView();

        buttonLogout.setOnClickListener(v -> logoutUser());

        fabAddKonto.setOnClickListener(v -> {
            // TODO: Implementacja dodawania nowego konta (np. otwarcie nowego dialogu/aktywności)
            showAddKontoDialog(); // Przykładowa metoda
            // Toast.makeText(KontaActivity.this, "Dodawanie nowego konta (do implementacji)", Toast.LENGTH_SHORT).show();
        });

        if (tokenManager.hasToken()) {
            fetchKonta();
        } else {
            redirectToLogin();
        }
    }

    private void setupRecyclerView() {
        kontaAdapter = new KontaAdapter(this, kontoList, this);
        recyclerViewKonta.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewKonta.setAdapter(kontaAdapter);
    }

    private void fetchKonta() {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Błąd autoryzacji.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        Call<List<KontoOdpowiedz>> call = apiService.przeslijMojeKonta("Bearer " + token);
        call.enqueue(new Callback<List<KontoOdpowiedz>>() {
            @Override
            public void onResponse(Call<List<KontoOdpowiedz>> call, Response<List<KontoOdpowiedz>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    kontoList.clear();
                    kontoList.addAll(response.body());
                    kontaAdapter.setKonta(kontoList);
                    Log.d(TAG, "Pobrano kont: " + kontoList.size());
                } else {
                    Log.e(TAG, "Błąd pobierania kont: " + response.code() + " " + response.message());
                    if (response.code() == 401 || response.code() == 403) {
                        Toast.makeText(KontaActivity.this, "Sesja wygasła. Zaloguj się ponownie.", Toast.LENGTH_LONG).show();
                        logoutUser();
                    } else {
                        Toast.makeText(KontaActivity.this, "Nie udało się pobrać kont. Kod: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<KontoOdpowiedz>> call, Throwable t) {
                Log.e(TAG, "Błąd sieci przy pobieraniu kont", t);
                Toast.makeText(KontaActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onModifyKontoClicked(KontoOdpowiedz konto) {
        showModifyKontoDialog(konto);
    }

    @Override
    public void onShowTransactionHistoryClicked(KontoOdpowiedz konto) {
        Intent intent = new Intent(this, TransakcjeActivity.class);
        intent.putExtra(TransakcjeActivity.EXTRA_KONTO_ID, konto.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteKontoClicked(KontoOdpowiedz konto, int position) {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            redirectToLogin();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Usuń Konto")
                .setMessage("Czy na pewno chcesz usunąć konto \"" + konto.getNazwa() + "\"? Spowoduje to również usunięcie wszystkich powiązanych transakcji.")
                .setPositiveButton("Usuń", (dialog, which) -> {
                    // Założenie: W ApiSerwis masz metodę usunKonto(token, kontoId)
                    Call<Void> call = apiService.usunKonto("Bearer " + token, konto.getId());
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(KontaActivity.this, "Konto \"" + konto.getNazwa() + "\" usunięte", Toast.LENGTH_SHORT).show();
                                kontaAdapter.removeKontoAt(position);
                            } else {
                                Log.e(TAG, "Błąd usuwania konta: " + response.code() + " " + response.message());
                                Toast.makeText(KontaActivity.this, "Nie udało się usunąć konta. Kod: " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e(TAG, "Błąd sieci przy usuwaniu konta", t);
                            Toast.makeText(KontaActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }


    private void showModifyKontoDialog(KontoOdpowiedz kontoToModify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_modify_konto, null);
        builder.setView(dialogView);

        EditText editTextNazwa = dialogView.findViewById(R.id.editTextModifyKontoNazwa);
        Spinner spinnerTyp = dialogView.findViewById(R.id.spinnerKontoTyp);
        ArrayList<String> typyKonta = new ArrayList<>();
        for (TypKontaEnum typ : TypKontaEnum.values()) {
            typyKonta.add(typ.name());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typyKonta);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTyp.setAdapter(adapter);
        spinnerTyp.setSelection(0);



        builder.setTitle("Modyfikuj Konto")
                .setPositiveButton("Zapisz", (dialog, which) -> {
                    String nowaNazwa = editTextNazwa.getText().toString().trim();
                    String nowyTyp = spinnerTyp.getSelectedItem().toString().trim();

                    if (TextUtils.isEmpty(nowaNazwa) || TextUtils.isEmpty(nowyTyp)) {
                        Toast.makeText(this, "Nazwa i typ konta nie mogą być puste.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Utwórz obiekt żądania (KontoRequest)
                    KontoWysylanie kontoWysylanie = new KontoWysylanie();
                    kontoWysylanie.setNazwa(nowaNazwa);
                    kontoWysylanie.setTyp(nowyTyp);
                    // Ustaw inne pola, jeśli są modyfikowalne i istnieją w KontoRequest
                    // np. kontoRequest.setWaluta(kontoToModify.getWaluta()); // Jeśli waluta nie jest modyfikowalna, pomiń

                    updateKontoOnServer(kontoToModify.getId(), kontoWysylanie);
                })
                .setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAddKontoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        // Możemy reużyć dialog_modify_konto.xml lub stworzyć dedykowany dialog_add_konto.xml
        View dialogView = inflater.inflate(R.layout.dialog_modify_konto, null); // Załóżmy reużycie
        builder.setView(dialogView);

        EditText editTextNazwa = dialogView.findViewById(R.id.editTextModifyKontoNazwa);
        Spinner spinnerTyp = dialogView.findViewById(R.id.spinnerKontoTyp);
        ArrayList<String> typyKonta = new ArrayList<>();
        for (TypKontaEnum typ : TypKontaEnum.values()) {
            typyKonta.add(typ.name());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typyKonta);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTyp.setAdapter(adapter);
        spinnerTyp.setSelection(0); // Domyślny typ konta
        editTextNazwa.setHint("Nazwa nowego konta");


        builder.setTitle("Dodaj Nowe Konto")
                .setPositiveButton("Dodaj", (dialog, which) -> {
                    String nazwa = editTextNazwa.getText().toString().trim();
                    String typ = spinnerTyp.getSelectedItem().toString().trim();

                    if (TextUtils.isEmpty(nazwa) || TextUtils.isEmpty(typ)) {
                        Toast.makeText(this, "Nazwa i typ konta są wymagane.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    KontoWysylanie kontoWysylanie = new KontoWysylanie();
                    kontoWysylanie.setNazwa(nazwa);
                    kontoWysylanie.setTyp(typ);
                    // Ustaw inne wymagane pola dla nowego konta, np. walutę, bilans początkowy
                    // kontoRequest.setWaluta("PLN"); // Przykładowo
                    // kontoRequest.setBilans(BigDecimal.ZERO); // Przykładowo

                    createKontoOnServer(kontoWysylanie);
                })
                .setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createKontoOnServer(KontoWysylanie kontoWysylanie) {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            redirectToLogin();
            return;
        }

        // Założenie: W ApiSerwis masz metodę dodajKonto(token, kontoRequest)
        Call<KontoOdpowiedz> call = apiService.dodajKonto("Bearer " + token, kontoWysylanie);
        call.enqueue(new Callback<KontoOdpowiedz>() {
            @Override
            public void onResponse(Call<KontoOdpowiedz> call, Response<KontoOdpowiedz> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(KontaActivity.this, "Konto \"" + response.body().getNazwa() + "\" dodane pomyślnie", Toast.LENGTH_SHORT).show();
                    fetchKonta(); // Odśwież listę kont
                } else {
                    Log.e(TAG, "Błąd dodawania konta: " + response.code() + " " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) { Log.e(TAG, "Error parsing error body", e); }
                    Toast.makeText(KontaActivity.this, "Nie udało się dodać konta. Kod: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<KontoOdpowiedz> call, Throwable t) {
                Log.e(TAG, "Błąd sieci przy dodawaniu konta", t);
                Toast.makeText(KontaActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void updateKontoOnServer(int kontoId, KontoWysylanie kontoWysylanie) {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            redirectToLogin();
            return;
        }

        // Założenie: W ApiSerwis masz metodę zmienKonto(token, kontoId, kontoRequest)
        Call<KontoOdpowiedz> call = apiService.zmienKonto("Bearer " + token, kontoId, kontoWysylanie);
        call.enqueue(new Callback<KontoOdpowiedz>() {
            @Override
            public void onResponse(Call<KontoOdpowiedz> call, Response<KontoOdpowiedz> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(KontaActivity.this, "Konto zaktualizowane", Toast.LENGTH_SHORT).show();
                    // Zaktualizuj element na liście w adapterze
                    kontaAdapter.updateKonto(response.body());
                    // fetchKonta(); // Lub odśwież całą listę, jeśli preferowane
                } else {
                    Log.e(TAG, "Błąd aktualizacji konta: " + response.code() + " " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) { Log.e(TAG, "Error parsing error body", e); }
                    Toast.makeText(KontaActivity.this, "Nie udało się zaktualizować konta. Kod: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<KontoOdpowiedz> call, Throwable t) {
                Log.e(TAG, "Błąd sieci przy aktualizacji konta", t);
                Toast.makeText(KontaActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void logoutUser() {
        tokenManager.clearAuthToken();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(KontaActivity.this, LogowanieActivity.class);
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
            fetchKonta();
        } else {
            redirectToLogin();
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Domyślne zachowanie to powrót do poprzedniej aktywności na stosie
        return true; // Zwróć true, aby zasygnalizować, że zdarzenie zostało obsłużone
    }
}