package com.example.zarzdzanie_finansami.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.KontoRequest; // Potrzebne DTO do wysyłania danych
import com.example.zarzdzanie_finansami.dto.KontoResponse;
import com.example.zarzdzanie_finansami.network.ApiSerwis;
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
    private List<KontoResponse> kontoList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konta);

        Toolbar toolbar = findViewById(R.id.toolbar_konta);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Moje Konta");
        }

        recyclerViewKonta = findViewById(R.id.recyclerViewKonta);
        buttonLogout = findViewById(R.id.buttonLogoutKonta);
        fabAddKonto = findViewById(R.id.fabAddKonto);

        tokenManager = new TokenMenadzer(this);
        apiService = RetrofitKlient.getClient().create(ApiSerwis.class);

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

        Call<List<KontoResponse>> call = apiService.getMojeKonta("Bearer " + token);
        call.enqueue(new Callback<List<KontoResponse>>() {
            @Override
            public void onResponse(Call<List<KontoResponse>> call, Response<List<KontoResponse>> response) {
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
            public void onFailure(Call<List<KontoResponse>> call, Throwable t) {
                Log.e(TAG, "Błąd sieci przy pobieraniu kont", t);
                Toast.makeText(KontaActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onModifyKontoClicked(KontoResponse konto) {
        showModifyKontoDialog(konto);
    }

    @Override
    public void onShowTransactionHistoryClicked(KontoResponse konto) {
        Intent intent = new Intent(this, TransakcjeActivity.class);
        intent.putExtra(TransakcjeActivity.EXTRA_KONTO_ID, konto.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteKontoClicked(KontoResponse konto, int position) {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            redirectToLogin();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Usuń Konto")
                .setMessage("Czy na pewno chcesz usunąć konto \"" + konto.getNazwa() + "\"? Spowoduje to również usunięcie wszystkich powiązanych transakcji.")
                .setPositiveButton("Usuń", (dialog, which) -> {
                    // Założenie: W ApiSerwis masz metodę deleteKonto(token, kontoId)
                    Call<Void> call = apiService.deleteKonto("Bearer " + token, konto.getId());
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


    private void showModifyKontoDialog(KontoResponse kontoToModify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_modify_konto, null);
        builder.setView(dialogView);

        EditText editTextNazwa = dialogView.findViewById(R.id.editTextModifyKontoNazwa);
        EditText editTextTyp = dialogView.findViewById(R.id.editTextModifyKontoTyp);

        editTextNazwa.setText(kontoToModify.getNazwa());
        editTextTyp.setText(kontoToModify.getTyp());

        builder.setTitle("Modyfikuj Konto")
                .setPositiveButton("Zapisz", (dialog, which) -> {
                    String nowaNazwa = editTextNazwa.getText().toString().trim();
                    String nowyTyp = editTextTyp.getText().toString().trim();

                    if (TextUtils.isEmpty(nowaNazwa) || TextUtils.isEmpty(nowyTyp)) {
                        Toast.makeText(this, "Nazwa i typ konta nie mogą być puste.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Utwórz obiekt żądania (KontoRequest)
                    KontoRequest kontoRequest = new KontoRequest();
                    kontoRequest.setNazwa(nowaNazwa);
                    kontoRequest.setTyp(nowyTyp);
                    // Ustaw inne pola, jeśli są modyfikowalne i istnieją w KontoRequest
                    // np. kontoRequest.setWaluta(kontoToModify.getWaluta()); // Jeśli waluta nie jest modyfikowalna, pomiń

                    updateKontoOnServer(kontoToModify.getId(), kontoRequest);
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
        EditText editTextTyp = dialogView.findViewById(R.id.editTextModifyKontoTyp);
        // Można dodać pole na bilans początkowy i walutę, jeśli serwer to obsługuje przy tworzeniu

        editTextNazwa.setHint("Nazwa nowego konta");
        editTextTyp.setHint("Typ nowego konta");


        builder.setTitle("Dodaj Nowe Konto")
                .setPositiveButton("Dodaj", (dialog, which) -> {
                    String nazwa = editTextNazwa.getText().toString().trim();
                    String typ = editTextTyp.getText().toString().trim();

                    if (TextUtils.isEmpty(nazwa) || TextUtils.isEmpty(typ)) {
                        Toast.makeText(this, "Nazwa i typ konta są wymagane.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    KontoRequest kontoRequest = new KontoRequest();
                    kontoRequest.setNazwa(nazwa);
                    kontoRequest.setTyp(typ);
                    // Ustaw inne wymagane pola dla nowego konta, np. walutę, bilans początkowy
                    // kontoRequest.setWaluta("PLN"); // Przykładowo
                    // kontoRequest.setBilans(BigDecimal.ZERO); // Przykładowo

                    createKontoOnServer(kontoRequest);
                })
                .setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createKontoOnServer(KontoRequest kontoRequest) {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            redirectToLogin();
            return;
        }

        // Założenie: W ApiSerwis masz metodę addKonto(token, kontoRequest)
        Call<KontoResponse> call = apiService.addKonto("Bearer " + token, kontoRequest);
        call.enqueue(new Callback<KontoResponse>() {
            @Override
            public void onResponse(Call<KontoResponse> call, Response<KontoResponse> response) {
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
            public void onFailure(Call<KontoResponse> call, Throwable t) {
                Log.e(TAG, "Błąd sieci przy dodawaniu konta", t);
                Toast.makeText(KontaActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void updateKontoOnServer(int kontoId, KontoRequest kontoRequest) {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            redirectToLogin();
            return;
        }

        // Założenie: W ApiSerwis masz metodę updateKonto(token, kontoId, kontoRequest)
        Call<KontoResponse> call = apiService.updateKonto("Bearer " + token, kontoId, kontoRequest);
        call.enqueue(new Callback<KontoResponse>() {
            @Override
            public void onResponse(Call<KontoResponse> call, Response<KontoResponse> response) {
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
            public void onFailure(Call<KontoResponse> call, Throwable t) {
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
        Intent intent = new Intent(KontaActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (tokenManager.hasToken()) {
            fetchKonta(); // Odśwież listę kont po powrocie do aktywności
        } else {
            redirectToLogin();
        }
    }
}