package com.example.zarzdzanie_finansami.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.budzet.BudzetOdpowiedz;
import com.example.zarzdzanie_finansami.network.api.ApiBudzet;
import com.example.zarzdzanie_finansami.network.api.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;
import com.example.zarzdzanie_finansami.ui.Adaptery.BudzetAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudzetActivity extends AppCompatActivity implements BudzetAdapter.OnBudzetInteractionListener {

    private static final String TAG = "BudzetActivity";
    private static final int ADD_BUDZET_REQUEST_CODE = 1;
    private static final int EDIT_BUDZET_REQUEST_CODE = 2;

    private RecyclerView recyclerViewBudzet;
    private FloatingActionButton fabAddBudzet;
    private ProgressBar progressBar;
    private ApiSerwis apiService;
    private TokenMenadzer tokenManager;
    private BudzetAdapter budzetAdapter;
    private List<BudzetOdpowiedz> budzetList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budzet);

        Toolbar toolbar = findViewById(R.id.toolbar_budzet);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Moje Budżety");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewBudzet = findViewById(R.id.recyclerViewBudzet);
        fabAddBudzet = findViewById(R.id.fabAddBudzet);
        progressBar = findViewById(R.id.progressBar);

        tokenManager = new TokenMenadzer(this);
        apiService = RetrofitKlient.getClient(this).create(ApiSerwis.class);

        setupRecyclerView();

        fabAddBudzet.setOnClickListener(v -> {
            Intent intent = new Intent(BudzetActivity.this, DodajBudzetActivity.class);
            startActivityForResult(intent, ADD_BUDZET_REQUEST_CODE);
        });

        if (tokenManager.hasToken()) {
            pobierzBudzety();
        } else {
            Toast.makeText(this, "Sesja Wygasła. Zaloguj się ponownie.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LogowanieActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void setupRecyclerView() {
        budzetAdapter = new BudzetAdapter(this, budzetList, this);
        recyclerViewBudzet.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBudzet.setAdapter(budzetAdapter);
    }

    private void pobierzBudzety() {
        progressBar.setVisibility(View.VISIBLE);
        String token = tokenManager.getAuthToken();
        if (token == null) return;

        apiService.pobierzBudzety("Bearer " + token).enqueue(new Callback<List<BudzetOdpowiedz>>() {
            @Override
            public void onResponse(Call<List<BudzetOdpowiedz>> call, Response<List<BudzetOdpowiedz>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    budzetList.clear();
                    budzetList.addAll(response.body());
                    budzetAdapter.setBudzety(budzetList);
                    Log.d(TAG, "Pobrano budżety: " + budzetList.size());
                } else {
                    Log.e(TAG, "Błąd pobierania budżetów: " + response.code());
                    Toast.makeText(BudzetActivity.this, "Błąd pobierania danych", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BudzetOdpowiedz>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Błąd sieci: ", t);
                Toast.makeText(BudzetActivity.this, "Błąd połączenia z siecią", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBudzetClicked(BudzetOdpowiedz budzet) {
        Toast.makeText(this, "Kliknięto: " + budzet.getNazwa(), Toast.LENGTH_SHORT).show();
        // Tutaj możesz otworzyć nową aktywność ze szczegółami budżetu
         Intent intent = new Intent(this, SzczegolyBudzetuActivity.class);
         intent.putExtra(SzczegolyBudzetuActivity.EXTRA_BUDZET_ID, budzet);
         startActivity(intent);
    }

    public void onBudzetLongClicked(final BudzetOdpowiedz budzet) {
        final CharSequence[] options = {"Edytuj Budżet", "Usuń Budżet", "Anuluj"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opcje dla: " + budzet.getNazwa());
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Edytuj Budżet")) {
                // Logika edycji budżetu
                Intent intent = new Intent(BudzetActivity.this, EdytujBudzetActivity.class); // Załóżmy, że masz taką aktywność
                intent.putExtra("BUDZET_DO_EDYCJI", budzet); // Przekaż cały obiekt budżetu do edycji
                // Lub tylko ID, jeśli aktywność edycji sama pobiera szczegóły
                // intent.putExtra("BUDZET_ID_DO_EDYCJI", budzet.getId());
                startActivityForResult(intent, EDIT_BUDZET_REQUEST_CODE);
                Toast.makeText(this, "Edytowanie: " + budzet.getNazwa(), Toast.LENGTH_SHORT).show();

            } else if (options[item].equals("Usuń Budżet")) {
                // Wywołaj metodę, którą już masz lub lekko zmodyfikuj
                onDeleteBudzetClicked(budzet); // Ta metoda już pokazuje dialog potwierdzający

            } else if (options[item].equals("Anuluj")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onManageBudgetItemsClicked(BudzetOdpowiedz budzet) {
        Intent intent = new Intent(this, SzczegolyBudzetuActivity.class);
        intent.putExtra(SzczegolyBudzetuActivity.EXTRA_BUDZET_ID, budzet.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteBudzetClicked(BudzetOdpowiedz budzet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usuwanie Budżetu")
                .setMessage("Czy na pewno chcesz usunąć budżet '" + budzet.getNazwa() + "'?")
                .setPositiveButton("Tak", (dialog, which) -> {
                    usunBudzetFromServer(budzet); // Zmieniona nazwa dla jasności, wywołuje API
                    dialog.dismiss();
                })
                .setNegativeButton("Nie", (dialog, which) -> dialog.dismiss())
                .show();
        // Usunięcie Toastu stąd, bo jest już w onResponse/onFailure usunBudzetFromServer
    }

    // Zmieniona nazwa metody usunBudzet na usunBudzetFromServer dla większej przejrzystości
// i dodanie obsługi odpowiedzi serwera
    private void usunBudzetFromServer(final BudzetOdpowiedz budzet) {
        String token = tokenManager.getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Błąd autoryzacji.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE); // Pokaż progressBar podczas usuwania

        // Załóżmy, że metoda usunBudzet w ApiSerwis istnieje i przyjmuje ID budżetu
        Call<Void> call = apiService.usunBudzet("Bearer " + token, budzet.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE); // Ukryj progressBar
                if (response.isSuccessful()) {
                    Toast.makeText(BudzetActivity.this, "Budżet '" + budzet.getNazwa() + "' został usunięty.", Toast.LENGTH_SHORT).show();
                    // Usuń budżet z listy lokalnej i odśwież RecyclerView
                    budzetList.remove(budzet);
                    budzetAdapter.notifyDataSetChanged();
                    // Alternatywnie, pobierz całą listę od nowa, jeśli serwer zmienia kolejność lub inne dane
                    // pobierzBudzety();
                } else {
                    Toast.makeText(BudzetActivity.this, "Błąd podczas usuwania budżetu. Kod: " + response.code(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Błąd usuwania budżetu: " + response.code() + " " + response.message());
                    // Możesz dodać bardziej szczegółową obsługę błędów, np. analizując ciało błędu response.errorBody()
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE); // Ukryj progressBar
                Toast.makeText(BudzetActivity.this, "Błąd sieci podczas usuwania budżetu: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Błąd sieci przy usuwaniu budżetu", t);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_BUDZET_REQUEST_CODE) {
                // Odśwież listę budżetów po pomyślnym dodaniu nowego
                pobierzBudzety();
            } else if (requestCode == EDIT_BUDZET_REQUEST_CODE) {
                // Odśwież listę budżetów po pomyślnej edycji
                pobierzBudzety();
            }
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
            pobierzBudzety();
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