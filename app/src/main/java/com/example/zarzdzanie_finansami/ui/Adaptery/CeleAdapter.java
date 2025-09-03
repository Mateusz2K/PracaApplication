package com.example.zarzdzanie_finansami.ui.Adaptery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.dto.cel.CelOdpowiedz; // Zmień na swój pakiet
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CeleAdapter extends RecyclerView.Adapter<CeleAdapter.CelViewHolder> {

    private Context context;
    private List<CelOdpowiedz> celeList;
    private OnCelInteractionListener listener;
    private final NumberFormat currencyFormatter;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");


    public interface OnCelInteractionListener {
        void onEdytujCelClicked(CelOdpowiedz cel, int position);
        void onUsunCelClicked(CelOdpowiedz cel, int position);
        void onDodajSrodkiClicked(CelOdpowiedz cel, int position);
    }

    public CeleAdapter(Context context, List<CelOdpowiedz> celeList, OnCelInteractionListener listener) {
        this.context = context;
        this.celeList = celeList;
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pl", "PL"));
    }

    @NonNull
    @Override
    public CelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cel, parent, false);
        return new CelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CelViewHolder holder, int position) {
        CelOdpowiedz cel = celeList.get(position);

        holder.textViewNazwaCelu.setText(cel.getNazwaCelu());
        holder.textViewOpisCelu.setText(cel.getOpis() != null ? cel.getOpis() : "");
        holder.textViewAktualnaKwotaCelu.setText(currencyFormatter.format(cel.getAktualnaKwota()));
        holder.textViewKwotaDocelowaCelu.setText(" / " + currencyFormatter.format(cel.getKwotaDocelowa()));

        if (cel.getDataZakonczenia() != null) {
            holder.textViewDataZakonczeniaCelu.setText("Do: " + cel.getDataZakonczenia().format(dateFormatter));
            holder.textViewDataZakonczeniaCelu.setVisibility(View.VISIBLE);
        } else {
            holder.textViewDataZakonczeniaCelu.setVisibility(View.GONE);
        }

        holder.progressBarCel.setProgress((int) cel.getProcentOsiagniety());

        TextView textViewStatusCelu = holder.itemView.findViewById(R.id.textViewStatusCelu); // Upewnij się, że masz to ID
        if (textViewStatusCelu != null && cel.getStatus() != null) {
            String statusText;
            statusText = cel.getStatus().name().replace("_", " "); // np. ZAKOŃCZONY -> ZAKOŃCZONY

            // Jeśli cel.getStatus() zwraca String bezpośrednio z JSON:
            // CelStatusEnumAndroid statusEnum = CelStatusEnumAndroid.fromString(cel.getStatus());
            // statusText = statusEnum.name().replace("_", " ");


            textViewStatusCelu.setText("Status: " + statusText);
            textViewStatusCelu.setVisibility(View.VISIBLE);

            // Zmiana koloru/wyglądu w zależności od statusu
            // (Jeśli używasz enuma CelStatusEnumAndroid w CelOdpowiedzDTO)
            switch (cel.getStatus()) {
                case AKTYWNY:
                    holder.itemView.setAlpha(1.0f); // Pełna widoczność
                    holder.buttonDodajSrodkiDoCelu.setEnabled(true);
                    // Możesz ustawić specyficzny kolor dla aktywnych
                    // textViewStatusCelu.setTextColor(ContextCompat.getColor(context, R.color.status_aktywny));
                    break;
                case ZAKOŃCZONY:
                    holder.itemView.setAlpha(0.7f); // Lekko przygaszony
                    holder.buttonDodajSrodkiDoCelu.setEnabled(false); // Nie można dodawać środków do zakończonego
                    textViewStatusCelu.setTextColor(ContextCompat.getColor(context, R.color.green_profit)); // Przykład koloru
                    break;
                case PRZETERMINOWANY:
                    holder.itemView.setAlpha(0.8f);
                    holder.buttonDodajSrodkiDoCelu.setEnabled(true); // Można nadal próbować zasilić
                    textViewStatusCelu.setTextColor(ContextCompat.getColor(context, R.color.red_loss)); // Przykład koloru
                    break;
                case NIEZNANY: // Jeśli dodałeś taki case do enuma
                default:
                    holder.itemView.setAlpha(1.0f);
                    holder.buttonDodajSrodkiDoCelu.setEnabled(true);
                    // Domyślny kolor
                    break;
            }
        } else if (textViewStatusCelu != null) {
            textViewStatusCelu.setVisibility(View.GONE);
        }

        holder.buttonDodajSrodkiDoCelu.setOnClickListener(v -> {
            if (listener != null) listener.onDodajSrodkiClicked(cel, holder.getAdapterPosition());
        });
        holder.buttonEdytujCel.setOnClickListener(v -> {
            if (listener != null) listener.onEdytujCelClicked(cel, holder.getAdapterPosition());
        });
        holder.buttonUsunCel.setOnClickListener(v -> {
            if (listener != null) listener.onUsunCelClicked(cel, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return celeList.size();
    }

    public void setCele(List<CelOdpowiedz> noweCele) {
        this.celeList.clear();
        if (noweCele != null) {
            this.celeList.addAll(noweCele);
        }
        notifyDataSetChanged();
    }

    static class CelViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNazwaCelu, textViewOpisCelu, textViewStatusCelu, textViewAktualnaKwotaCelu, textViewKwotaDocelowaCelu, textViewDataZakonczeniaCelu;
        ProgressBar progressBarCel;
        Button buttonDodajSrodkiDoCelu;
        ImageButton buttonEdytujCel, buttonUsunCel;

        public CelViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNazwaCelu = itemView.findViewById(R.id.textViewNazwaCelu);
            textViewOpisCelu = itemView.findViewById(R.id.textViewOpisCelu);
            textViewAktualnaKwotaCelu = itemView.findViewById(R.id.textViewAktualnaKwotaCelu);
            textViewKwotaDocelowaCelu = itemView.findViewById(R.id.textViewKwotaDocelowaCelu);
            textViewDataZakonczeniaCelu = itemView.findViewById(R.id.textViewDataZakonczeniaCelu);
            textViewStatusCelu = itemView.findViewById(R.id.textViewStatusCelu); // <<-- INICJALIZACJA
            progressBarCel = itemView.findViewById(R.id.progressBarCel);
            buttonDodajSrodkiDoCelu = itemView.findViewById(R.id.buttonDodajSrodkiDoCelu);
            buttonEdytujCel = itemView.findViewById(R.id.buttonEdytujCel);
            buttonUsunCel = itemView.findViewById(R.id.buttonUsunCel);
        }
    }
}


