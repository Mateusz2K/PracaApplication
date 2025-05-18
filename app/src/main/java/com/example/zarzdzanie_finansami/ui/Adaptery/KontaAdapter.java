package com.example.zarzdzanie_finansami.ui.Adaptery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.dto.KontoResponse;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class KontaAdapter extends RecyclerView.Adapter<KontaAdapter.KontoViewHolder> {

    private List<KontoResponse> kontoList;
    private final OnKontoInteractionListener listener;
    private final Context context;
    private NumberFormat currencyFormatter;


    public interface OnKontoInteractionListener {
        void onModifyKontoClicked(KontoResponse konto);
        void onShowTransactionHistoryClicked(KontoResponse konto);
        void onDeleteKontoClicked(KontoResponse konto, int position); // Dodana opcja usuwania
    }

    public KontaAdapter(Context context, List<KontoResponse> kontoList, OnKontoInteractionListener listener) {
        this.context = context;
        this.kontoList = kontoList;
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pl", "PL")); // Domyślne formatowanie PLN
    }

    @NonNull
    @Override
    public KontoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_konta, parent, false);
        return new KontoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull KontoViewHolder holder, int position) {
        KontoResponse currentKonto = kontoList.get(position);

        holder.textViewKontoNazwa.setText(currentKonto.getNazwa());
        holder.textViewKontoTyp.setText("Typ: " + currentKonto.getTyp());

        // Formatowanie bilansu
        if (currentKonto.getBilans() != null) {
            // Ustaw walutę dynamicznie, jeśli jest dostępna, inaczej domyślna
            String waluta = currentKonto.getWaluta() != null ? currentKonto.getWaluta() : "PLN";
            // Chociaż NumberFormat.getCurrencyInstance() używa symbolu waluty z Locale,
            // możemy chcieć jawnie pokazać kod waluty, jeśli się różni od domyślnego Locale.
            // Na razie formatter użyje symbolu dla PLN.
            String formattedBilans = currencyFormatter.format(currentKonto.getBilans());
            holder.textViewKontoBilans.setText("Bilans: " + formattedBilans + (waluta.equals("PLN") ? "" : " " + waluta) );
        } else {
            holder.textViewKontoBilans.setText("Bilans: Brak danych");
        }


        holder.buttonKontoOptions.setOnClickListener(v -> showPopupMenu(holder.buttonKontoOptions, currentKonto, position));
    }

    private void showPopupMenu(View view, KontoResponse konto, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.menu_konto_options); // Utworzymy ten plik menu poniżej
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_modify_konto) {
                listener.onModifyKontoClicked(konto);
                return true;
            } else if (itemId == R.id.action_show_history) {
                listener.onShowTransactionHistoryClicked(konto);
                return true;
            } else if (itemId == R.id.action_delete_konto) {
                listener.onDeleteKontoClicked(konto, position);
                return true;
            }
            return false;
        });
        popup.show();
    }


    @Override
    public int getItemCount() {
        return kontoList == null ? 0 : kontoList.size();
    }

    public void setKonta(List<KontoResponse> konta) {
        this.kontoList = konta;
        notifyDataSetChanged(); // Dla prostoty, można użyć DiffUtil dla lepszej wydajności
    }

    public void removeKontoAt(int position) {
        if (position >= 0 && position < kontoList.size()) {
            kontoList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateKonto(KontoResponse updatedKonto) {
        for (int i = 0; i < kontoList.size(); i++) {
            if (kontoList.get(i).getId() == updatedKonto.getId()) {
                kontoList.set(i, updatedKonto);
                notifyItemChanged(i);
                return;
            }
        }
    }


    static class KontoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewKontoNazwa, textViewKontoBilans, textViewKontoTyp;
        ImageButton buttonKontoOptions;

        KontoViewHolder(View view) {
            super(view);
            textViewKontoNazwa = view.findViewById(R.id.textViewKontoNazwa);
            textViewKontoBilans = view.findViewById(R.id.textViewKontoBilans);
            textViewKontoTyp = view.findViewById(R.id.textViewKontoTyp);
            buttonKontoOptions = view.findViewById(R.id.buttonKontoOptions);
        }
    }
}