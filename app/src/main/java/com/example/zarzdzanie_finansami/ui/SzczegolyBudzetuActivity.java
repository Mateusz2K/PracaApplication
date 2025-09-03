package com.example.zarzdzanie_finansami.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.budzet.BudzetOdpowiedz;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;
import com.example.zarzdzanie_finansami.network.api.ApiBudzet;
import com.example.zarzdzanie_finansami.network.api.ApiSerwis;
import com.example.zarzdzanie_finansami.ui.Adaptery.PozycjeBudzetuAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SzczegolyBudzetuActivity extends AppCompatActivity {

    public static final String EXTRA_BUDZET_ID = "extra_budzet_id";
    private Long budzetId;

    private ApiBudzet apiService;
    private TokenMenadzer tokenManager;

    private RecyclerView recyclerView;
    private PozycjeBudzetuAdapter adapter;
    private TextView textViewNazwa, textViewDochod;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_szczegoly_budzet);

        budzetId = getIntent().getLongExtra(EXTRA_BUDZET_ID, -1L);
        if (budzetId == -1L) {
            Toast.makeText(this, "Nieprawidłowy ID budżetu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar_szczegoly_budzetu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        apiService = RetrofitKlient.getClient(this).create(ApiBudzet.class);
        tokenManager = new TokenMenadzer(this);

        textViewNazwa = findViewById(R.id.textViewSzczegolyNazwa);
        textViewDochod = findViewById(R.id.textViewSzczegolyDochod);
        recyclerView = findViewById(R.id.recyclerViewPozycjeBudzetu);

        setupRecyclerView();
        fetchBudzetDetails();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PozycjeBudzetuAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void fetchBudzetDetails() {
        String token = "Bearer " + tokenManager.getAuthToken();
        apiService.pobierzBudzet(token, budzetId).enqueue(new Callback<BudzetOdpowiedz>() {
            @Override
            public void onResponse(Call<BudzetOdpowiedz> call, Response<BudzetOdpowiedz> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BudzetOdpowiedz budzet = response.body();
                    textViewNazwa.setText(budzet.getNazwa());
                    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pl", "PL"));
                    textViewDochod.setText("Dochód: " + currencyFormatter.format(budzet.getPrzewidywanyDochod()));

                    if (budzet.getPozycjeBudzetu() != null) {
                        adapter.setPozycje(budzet.getPozycjeBudzetu());
                    }
                } else {
                    Toast.makeText(SzczegolyBudzetuActivity.this, "Błąd pobierania danych: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BudzetOdpowiedz> call, Throwable t) {
                Toast.makeText(SzczegolyBudzetuActivity.this, "Błąd sieci", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}