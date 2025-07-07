package com.example.zarzdzanie_finansami.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.TransakcjaOdpowiedz; // Upewnij się, że ta klasa istnieje i ma pola
import com.example.zarzdzanie_finansami.network.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;
import com.example.zarzdzanie_finansami.ui.Adaptery.TransakcjeAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransakcjeActivity extends AppCompatActivity implements TransakcjeAdapter.OnTransactionListener {
    private static final String TAG = "TransakcjeActivity";
    public static final String EXTRA_KONTO_ID = "extra_konto_id";


    private RecyclerView recyclerViewTransactions;
    private Button buttonLogout;
    private FloatingActionButton fabAddTransaction;
    private ApiSerwis apiService;
    private TokenMenadzer tokenManager;
    private TransakcjeAdapter transactionsAdapter;
    private List<TransakcjaOdpowiedz> transactionList = new ArrayList<>();
    private int kontoId; // ID konta, dla którego wyświetlamy transakcje


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) { // Poprawiona sygnatura onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transakcje);

        Toolbar toolbar = findViewById(R.id.toolbar_transakcje);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Transakcje");
            // Możesz dodać przycisk wstecz, jeśli ta aktywność nie jest główną
            // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        // Pobierz ID konta przekazane z KontaActivity
        kontoId = getIntent().getIntExtra(EXTRA_KONTO_ID, -1);
        if (kontoId == -1) {
            Toast.makeText(this, "Nieprawidłowe ID konta", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Nie przekazano ID konta do TransakcjeActivity");
            finish(); // Zamknij aktywność, jeśli ID konta jest nieprawidłowe
            return;
        }


        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
        buttonLogout = findViewById(R.id.buttonLogoutTransakcje); // Zmienione ID na zgodne z layoutem
        fabAddTransaction = findViewById(R.id.fabAddTransaction);

        tokenManager = new TokenMenadzer(this);
        apiService = RetrofitKlient.getClient().create(ApiSerwis.class);

        setupRecyclerView();

        buttonLogout.setOnClickListener(v -> logoutUser());
        fabAddTransaction.setOnClickListener(v -> {
            // Tutaj logika otwierania nowej aktywności/dialogu do dodawania transakcji
            // Np. Intent intent = new Intent(TransakcjeActivity.this, AddTransactionActivity.class);
            // intent.putExtra(AddTransactionActivity.EXTRA_KONTO_ID, kontoId);
            // startActivity(intent);
            Toast.makeText(this, "Dodawanie nowej transakcji (do implementacji)", Toast.LENGTH_SHORT).show();
        });

        if (tokenManager.hasToken()) {
            fetchTransactions();
        } else {
            redirectToLogin();
        }
    }

    private void setupRecyclerView() {
        transactionsAdapter = new TransakcjeAdapter(transactionList, this);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(transactionsAdapter);
    }

    private void fetchTransactions() {
        String token = tokenManager.getAuthToken();
        if (token == null || kontoId == -1) {
            Toast.makeText(this, "Błąd autoryzacji lub brak ID konta.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Założenie: W ApiSerwis masz metodę getTransakcjeDlaKonta(token, kontoId)
        Call<List<TransakcjaOdpowiedz>> call = apiService.getTransakcjeDlaKonta("Bearer " + token, kontoId);
        call.enqueue(new Callback<List<TransakcjaOdpowiedz>>() {
            @Override
            public void onResponse(Call<List<TransakcjaOdpowiedz>> call, Response<List<TransakcjaOdpowiedz>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transactionList.clear();
                    transactionList.addAll(response.body());
                    transactionsAdapter.setTransactions(transactionList); // Użyj metody setTransactions
                    Log.d(TAG, "Pobrano transakcji: " + transactionList.size());
                } else {
                    Log.e(TAG, "Błąd pobierania transakcji: " + response.code() + " " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Błąd parsowania error body", e);
                    }
                    if (response.code() == 401 || response.code() == 403) {
                        Toast.makeText(TransakcjeActivity.this, "Sesja wygasła. Zaloguj się ponownie.", Toast.LENGTH_LONG).show();
                        logoutUser();
                    } else {
                        Toast.makeText(TransakcjeActivity.this, "Nie udało się pobrać transakcji. Kod: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TransakcjaOdpowiedz>> call, Throwable t) {
                Log.e(TAG, "Błąd sieci przy pobieraniu transakcji", t);
                Toast.makeText(TransakcjeActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onModifyClick(TransakcjaOdpowiedz transakcja) {
        // Tutaj logika otwierania nowej aktywności/dialogu do modyfikacji transakcji
        // Np. Intent intent = new Intent(this, ModifyTransactionActivity.class);
        // intent.putExtra("TRANSACTION_ID", transakcja.getId()); // Przekaż ID transakcji
        // startActivity(intent);
        Toast.makeText(this, "Modyfikacja transakcji: " + transakcja.getOpis() + " (do implementacji)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int transakcjaId, int position) {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Błąd autoryzacji.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Wyświetl dialog potwierdzający usunięcie
        new android.app.AlertDialog.Builder(this)
                .setTitle("Usuń transakcję")
                .setMessage("Czy na pewno chcesz usunąć tę transakcję?")
                .setPositiveButton("Usuń", (dialog, which) -> {
                    // Założenie: W ApiSerwis masz metodę deleteTransakcja(token, transakcjaId)
                    Call<Void> call = apiService.deleteTransakcja("Bearer " + token, transakcjaId);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(TransakcjeActivity.this, "Transakcja usunięta", Toast.LENGTH_SHORT).show();
                                // Usuń z listy w adapterze
                                transactionsAdapter.removeTransactionAt(position);
                            } else {
                                Log.e(TAG, "Błąd usuwania transakcji: " + response.code() + " " + response.message());
                                if (response.code() == 401 || response.code() == 403) {
                                    Toast.makeText(TransakcjeActivity.this, "Sesja wygasła. Zaloguj się ponownie.", Toast.LENGTH_LONG).show();
                                    logoutUser();
                                } else {
                                    Toast.makeText(TransakcjeActivity.this, "Nie udało się usunąć transakcji. Kod: " + response.code(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e(TAG, "Błąd sieci przy usuwaniu transakcji", t);
                            Toast.makeText(TransakcjeActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Anuluj", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void logoutUser() {
        tokenManager.clearAuthToken();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(TransakcjeActivity.this, LogowanieActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Odśwież listę transakcji, jeśli użytkownik wróci do tej aktywności
        // np. po dodaniu/modyfikacji transakcji
        if (tokenManager.hasToken() && kontoId != -1) {
            fetchTransactions();
        }
    }
}