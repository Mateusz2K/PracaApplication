package com.example.zarzdzanie_finansami.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.autoryzacja.TokenMenadzer;
import com.example.zarzdzanie_finansami.dto.kategoria.KategoriaOdpowiedz;
import com.example.zarzdzanie_finansami.dto.kategoria.KategoriaWysylanie;
import com.example.zarzdzanie_finansami.dto.transakcja.TypTransakcjiEnum;
import com.example.zarzdzanie_finansami.network.api.ApiSerwis;
import com.example.zarzdzanie_finansami.network.RetrofitKlient;
import com.example.zarzdzanie_finansami.ui.Adaptery.KategorieAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KategorieActivity extends AppCompatActivity implements KategorieAdapter.OnKategoriaInteractionListener {

    private static final String TAG = "KategorieActivity";
    private static final String TYP_KOSZT = "KOSZT";
    private static final String TYP_PRZYCHOD = "PRZYCHÓD";

    private Toolbar toolbar;
    private ChipGroup chipGroupTypKategorii;
    private Chip chipKategorieKoszt, chipKategoriePrzychod;
    private RecyclerView recyclerViewKategorie;
    private FloatingActionButton fabDodajKategorie;

    private KategorieAdapter kategorieAdapter;
    private List<KategoriaOdpowiedz> wszystkiePobraneKategorie = new ArrayList<>(); // Przechowuje wszystkie pobrane
    private List<KategoriaOdpowiedz> aktualnieWyswietlaneKategorie = new ArrayList<>(); // Dla adaptera

    private ApiSerwis apiService;
    private TokenMenadzer tokenManager;
    private String aktualnieWybranyTyp = TYP_KOSZT; // Domyślnie

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kategorie);

        toolbar = findViewById(R.id.toolbar_kategorie);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Zarządzaj Kategoriami");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chipGroupTypKategorii = findViewById(R.id.chipGroupTypKategorii);
        chipKategorieKoszt = findViewById(R.id.chipKategorieKoszt);
        chipKategoriePrzychod = findViewById(R.id.chipKategoriePrzychod);
        recyclerViewKategorie = findViewById(R.id.recyclerViewKategorie);
        fabDodajKategorie = findViewById(R.id.fabDodajKategorie);

        tokenManager = new TokenMenadzer(this);
        apiService = RetrofitKlient.getClient(this).create(ApiSerwis.class);

        setupRecyclerView();
        setupChipGroupListener();

        fabDodajKategorie.setOnClickListener(v -> showDialogEdycjiKategorii(null, -1));

        if (tokenManager.hasToken()) {
            pobierzKategorieZSerwera(); // Pobierz wszystkie na start
        } else {
            // Przekieruj do logowania
            Toast.makeText(this, "Sesja wygasła, zaloguj się ponownie.", Toast.LENGTH_LONG).show();
            // redirectToLogin();
        }
    }

    private void setupRecyclerView() {
        kategorieAdapter = new KategorieAdapter(this, aktualnieWyswietlaneKategorie, this);
        recyclerViewKategorie.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewKategorie.setAdapter(kategorieAdapter);
    }

    private void setupChipGroupListener() {
        chipGroupTypKategorii.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipKategorieKoszt) {
                aktualnieWybranyTyp = TYP_KOSZT;
            } else if (checkedId == R.id.chipKategoriePrzychod) {
                aktualnieWybranyTyp = TYP_PRZYCHOD;
            }
            filtrujIWyswietlKategorie();
        });
        // Ustawienie początkowego stanu
        chipKategorieKoszt.setChecked(true); // Wywoła listenera i załaduje kategorie kosztów
    }

    private void pobierzKategorieZSerwera() {
        String token = tokenManager.getAuthToken();
        if (token == null) return;

        // Użyj endpointu pobierającego wszystkie kategorie lub odpowiednio dostosuj
        apiService.pobierzWszystkieKategorie("Bearer " + token).enqueue(new Callback<List<KategoriaOdpowiedz>>() {
            @Override
            public void onResponse(Call<List<KategoriaOdpowiedz>> call, Response<List<KategoriaOdpowiedz>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    wszystkiePobraneKategorie.clear();
                    wszystkiePobraneKategorie.addAll(response.body());
                    filtrujIWyswietlKategorie(); // Filtruj na podstawie aktualnie wybranego chipa
                } else {
                    Toast.makeText(KategorieActivity.this, "Błąd pobierania kategorii: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<KategoriaOdpowiedz>> call, Throwable t) {
                Toast.makeText(KategorieActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrujIWyswietlKategorie() {
        aktualnieWyswietlaneKategorie.clear();
        if (wszystkiePobraneKategorie != null) {
            for (KategoriaOdpowiedz kat : wszystkiePobraneKategorie) {
                if (kat.getTypTransakcji() != null && kat.getTypTransakcji().equalsIgnoreCase(aktualnieWybranyTyp)) {
                    aktualnieWyswietlaneKategorie.add(kat);
                }
            }
        }
        kategorieAdapter.setKategorie(new ArrayList<>(aktualnieWyswietlaneKategorie)); // Przekaż kopię
    }


    private void showDialogEdycjiKategorii(@Nullable KategoriaOdpowiedz kategoriaDoModyfikacji, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edycja_kategorii, null);
        builder.setView(dialogView);

        TextView textViewTytul = dialogView.findViewById(R.id.textViewTytulDialoguKategorii);
        EditText editTextNazwa = dialogView.findViewById(R.id.editTextNazwaKategoriiDialog);
        TextView textViewTyp = dialogView.findViewById(R.id.textViewTypKategoriiDialog);

        final String typTransakcjiDlaKategorii = aktualnieWybranyTyp; // Pobierz z zaznaczonego Chipa
        textViewTyp.setText(typTransakcjiDlaKategorii);

        if (kategoriaDoModyfikacji != null) {
            textViewTytul.setText("Modyfikuj Kategorię");
            editTextNazwa.setText(kategoriaDoModyfikacji.getNazwa());
            // Typ jest już ustawiony na podstawie chipGroup
        } else {
            textViewTytul.setText("Dodaj Nową Kategorię");
        }

        builder.setPositiveButton(kategoriaDoModyfikacji != null ? "Zapisz" : "Dodaj", null); // Ustaw listener później, aby kontrolować zamykanie
        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button buttonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            buttonPositive.setOnClickListener(view -> {
                String nazwaKategorii = editTextNazwa.getText().toString().trim();
                if (TextUtils.isEmpty(nazwaKategorii)) {
                    editTextNazwa.setError("Nazwa nie może być pusta");
                    return;
                }

                KategoriaWysylanie dto = new KategoriaWysylanie(nazwaKategorii, TypTransakcjiEnum.valueOf(typTransakcjiDlaKategorii));
                if (kategoriaDoModyfikacji != null) {
                    modyfikujKategorieNaSerwerze(kategoriaDoModyfikacji.getId(), dto, position, dialog);
                } else {
                    dodajKategorieNaSerwerze(dto, dialog);
                }
            });
        });
        dialog.show();
    }

    private void dodajKategorieNaSerwerze(KategoriaWysylanie dto, AlertDialog dialogToDismiss) {
        String token = tokenManager.getAuthToken();
        if (token == null) return;

        apiService.dodajKategorie("Bearer " + token, dto).enqueue(new Callback<KategoriaOdpowiedz>() {
            @Override
            public void onResponse(Call<KategoriaOdpowiedz> call, Response<KategoriaOdpowiedz> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(KategorieActivity.this, "Kategoria dodana", Toast.LENGTH_SHORT).show();
                    wszystkiePobraneKategorie.add(response.body()); // Dodaj do głównej listy
                    filtrujIWyswietlKategorie(); // Odśwież widok
                    if (dialogToDismiss != null) dialogToDismiss.dismiss();
                } else {
                    Toast.makeText(KategorieActivity.this, "Błąd dodawania: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<KategoriaOdpowiedz> call, Throwable t) {
                Toast.makeText(KategorieActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void modyfikujKategorieNaSerwerze(int kategoriaId, KategoriaWysylanie dto, int position, AlertDialog dialogToDismiss) {
        String token = tokenManager.getAuthToken();
        if (token == null) return;

        apiService.modyfikujKategorie("Bearer " + token, kategoriaId, dto).enqueue(new Callback<KategoriaOdpowiedz>() {
            @Override
            public void onResponse(Call<KategoriaOdpowiedz> call, Response<KategoriaOdpowiedz> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(KategorieActivity.this, "Kategoria zaktualizowana", Toast.LENGTH_SHORT).show();
                    // Zaktualizuj w głównej liście
                    KategoriaOdpowiedz zaktualizowana = response.body();
                    int indexInAll = -1;
                    for (int i = 0; i < wszystkiePobraneKategorie.size(); i++) {
                        if (wszystkiePobraneKategorie.get(i).getId() == (zaktualizowana.getId())) {
                            indexInAll = i;
                            break;
                        }
                    }
                    if (indexInAll != -1) {
                        wszystkiePobraneKategorie.set(indexInAll, zaktualizowana);
                    }
                    filtrujIWyswietlKategorie(); // Odśwież widok
                    if (dialogToDismiss != null) dialogToDismiss.dismiss();
                } else {
                    Toast.makeText(KategorieActivity.this, "Błąd modyfikacji: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<KategoriaOdpowiedz> call, Throwable t) {
                Toast.makeText(KategorieActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onModyfikujKategorieClicked(KategoriaOdpowiedz kategoria, int position) {
        // Upewnij się, że typ w ChipGroup odpowiada typowi modyfikowanej kategorii,
        // chociaż dialog i tak pobierze typ z aktualnie zaznaczonego Chipa.
        if (!kategoria.getTypTransakcji().equalsIgnoreCase(aktualnieWybranyTyp)) {
            Toast.makeText(this, "Przełącz typ na " + kategoria.getTypTransakcji() + " aby edytować.", Toast.LENGTH_LONG).show();
            // Opcjonalnie: automatycznie przełącz ChipGroup
            // if (kategoria.getTyp().equalsIgnoreCase(TYP_KOSZT)) chipKategorieKoszt.setChecked(true);
            // else if (kategoria.getTyp().equalsIgnoreCase(TYP_PRZYCHOD)) chipKategoriePrzychod.setChecked(true);
            return;
        }
        showDialogEdycjiKategorii(kategoria, position);
    }

    @Override
    public void onUsunKategorieClicked(KategoriaOdpowiedz kategoria, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Usuń Kategorię")
                .setMessage("Czy na pewno chcesz usunąć kategorię \"" + kategoria.getNazwa() + "\"?")
                .setPositiveButton("Usuń", (dialog, which) -> usunKategorieZSerwera(kategoria, position))
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void usunKategorieZSerwera(KategoriaOdpowiedz kategoria, int positionInAdapter) {
        String token = tokenManager.getAuthToken();
        if (token == null) return;

        apiService.usunKategorie("Bearer " + token, kategoria.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(KategorieActivity.this, "Kategoria usunięta", Toast.LENGTH_SHORT).show();
                    // Usuń z głównej listy
                    wszystkiePobraneKategorie.removeIf(kat -> kat.getId() == (kategoria.getId()));
                    filtrujIWyswietlKategorie(); // Odśwież widok
                } else {
                    Toast.makeText(KategorieActivity.this, "Błąd usuwania: " + response.code() + ". Sprawdź, czy kategoria nie jest używana.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(KategorieActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
            pobierzKategorieZSerwera();
        } else {
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Sesja wygasła, zaloguj się ponownie.", Toast.LENGTH_LONG).show();
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

