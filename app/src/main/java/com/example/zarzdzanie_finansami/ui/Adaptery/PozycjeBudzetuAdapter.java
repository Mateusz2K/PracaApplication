package com.example.zarzdzanie_finansami.ui.Adaptery; // Lub odpowiedni pakiet

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.dto.budzet.PozycjaBudzetuOdpowiedz;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PozycjeBudzetuAdapter extends RecyclerView.Adapter<PozycjeBudzetuAdapter.ViewHolder> {

    private List<PozycjaBudzetuOdpowiedz> listaPozycji;
    private Context context;
    private NumberFormat currencyFormatter;
    private NumberFormat percentFormatter;

    public PozycjeBudzetuAdapter(Context context, List<PozycjaBudzetuOdpowiedz> listaPozycji) {
        this.context = context;
        this.listaPozycji = listaPozycji;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pl", "PL"));
        this.percentFormatter = NumberFormat.getPercentInstance();
        this.percentFormatter.setMaximumFractionDigits(0); // Procenty bez miejsc po przecinku
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pozycja_budzetu_szczegoly, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PozycjaBudzetuOdpowiedz pozycja = listaPozycji.get(position);

        holder.kategoriaNazwa.setText(pozycja.getKategoriaNazwa());

        StringBuilder alokacjaStr = new StringBuilder();
        if ("KWOTOWY".equalsIgnoreCase(pozycja.getTypAlokacji()) && pozycja.getKwotaAlokowana() != null) {
            alokacjaStr.append(currencyFormatter.format(pozycja.getKwotaAlokowana()));
        } else if ("PROCENTOWY".equalsIgnoreCase(pozycja.getTypAlokacji()) && pozycja.getProcentAlokowany() != null) {
            alokacjaStr.append(percentFormatter.format(pozycja.getProcentAlokowany().doubleValue() / 100.0)); // Zakładając, że procent jest np. 20 a nie 0.20
            if (pozycja.getKwotaAlokowana() != null) { // Jeśli jest też kwota wynikowa z procentu
                alokacjaStr.append(" (").append(currencyFormatter.format(pozycja.getKwotaAlokowana())).append(")");
            }
        } else if (pozycja.getKwotaAlokowana() != null) { // Fallback na kwotę, jeśli typ nieznany
            alokacjaStr.append(currencyFormatter.format(pozycja.getKwotaAlokowana()));
        } else {
            alokacjaStr.append("Brak danych");
        }
        holder.alokacjaWartosc.setText(alokacjaStr.toString());


        if (pozycja.getRzeczywisteWydatki() != null) {
            holder.wydatkiWartosc.setText(currencyFormatter.format(pozycja.getRzeczywisteWydatki()));
        } else {
            holder.wydatkiWartosc.setText(currencyFormatter.format(BigDecimal.ZERO));
        }

        if (pozycja.getPozostalo() != null) {
            holder.pozostaloWartosc.setText(currencyFormatter.format(pozycja.getPozostalo()));
        } else {
            holder.pozostaloWartosc.setText("Brak danych");
        }

        int progress = (int) Math.round(pozycja.getProcentWykorzystania());
        holder.progressBarWykorzystanie.setProgress(progress);
        holder.procentWykorzystania.setText(String.format(Locale.getDefault(),"Wykorzystano: %d%%", progress));

        // Możesz dodać logikę zmiany koloru paska postępu w zależności od procentu wykorzystania
        // np. jeśli > 80% to pomarańczowy, jeśli > 100% to czerwony
    }

    @Override
    public int getItemCount() {
        return listaPozycji == null ? 0 : listaPozycji.size();
    }

    public void setPozycje(List<PozycjaBudzetuOdpowiedz> nowePozycje) {
        this.listaPozycji = nowePozycje;
        notifyDataSetChanged(); // Dla prostoty, dla wydajności użyj DiffUtil
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView kategoriaNazwa, alokacjaWartosc, wydatkiWartosc, pozostaloWartosc, procentWykorzystania;
        ProgressBar progressBarWykorzystanie;

        ViewHolder(View itemView) {
            super(itemView);
            kategoriaNazwa = itemView.findViewById(R.id.textViewPozycjaKategoriaNazwa);
            alokacjaWartosc = itemView.findViewById(R.id.textViewPozycjaAlokacjaWartosc);
            wydatkiWartosc = itemView.findViewById(R.id.textViewPozycjaWydatkiWartosc);
            pozostaloWartosc = itemView.findViewById(R.id.textViewPozycjaPozostaloWartosc);
            procentWykorzystania = itemView.findViewById(R.id.textViewPozycjaProcentWykorzystania);
            progressBarWykorzystanie = itemView.findViewById(R.id.progressBarPozycjaWykorzystanie);
        }
    }
}

