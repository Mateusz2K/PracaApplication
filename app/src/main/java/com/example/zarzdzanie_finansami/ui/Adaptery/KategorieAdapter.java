package com.example.zarzdzanie_finansami.ui.Adaptery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.dto.kategoria.KategoriaOdpowiedz;
import java.util.List;

public class KategorieAdapter extends RecyclerView.Adapter<KategorieAdapter.KategoriaViewHolder> {

    private Context context;
    private List<KategoriaOdpowiedz> kategorieList;
    private OnKategoriaInteractionListener listener;

    public interface OnKategoriaInteractionListener {
        void onModyfikujKategorieClicked(KategoriaOdpowiedz kategoria, int position);
        void onUsunKategorieClicked(KategoriaOdpowiedz kategoria, int position);
    }

    public KategorieAdapter(Context context, List<KategoriaOdpowiedz> kategorieList, OnKategoriaInteractionListener listener) {
        this.context = context;
        this.kategorieList = kategorieList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public KategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_kategoria, parent, false);
        return new KategoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KategoriaViewHolder holder, int position) {
        KategoriaOdpowiedz kategoria = kategorieList.get(position);
        holder.textViewNazwaKategorii.setText(kategoria.getNazwa());

        holder.buttonModyfikujKategorie.setOnClickListener(v -> {
            if (listener != null) {
                listener.onModyfikujKategorieClicked(kategoria, holder.getAdapterPosition());
            }
        });

        holder.buttonUsunKategorie.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUsunKategorieClicked(kategoria, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return kategorieList.size();
    }

    public void setKategorie(List<KategoriaOdpowiedz> noweKategorie) {
        this.kategorieList.clear();
        this.kategorieList.addAll(noweKategorie);
        notifyDataSetChanged();
    }

    public void dodajKategorie(KategoriaOdpowiedz kategoria) {
        this.kategorieList.add(kategoria);
        notifyItemInserted(kategorieList.size() - 1);
    }

    public void aktualizujKategorie(KategoriaOdpowiedz kategoria, int position) {
        if (position >= 0 && position < kategorieList.size()) {
            kategorieList.set(position, kategoria);
            notifyItemChanged(position);
        }
    }

    public void usunKategorieAt(int position) {
        if (position >= 0 && position < kategorieList.size()) {
            kategorieList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, kategorieList.size()); // Aby zaktualizować pozycje
        }
    }

    static class KategoriaViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNazwaKategorii;
        ImageButton buttonModyfikujKategorie;
        ImageButton buttonUsunKategorie;

        public KategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNazwaKategorii = itemView.findViewById(R.id.textViewNazwaKategorii);
            buttonModyfikujKategorie = itemView.findViewById(R.id.buttonModyfikujKategorie);
            buttonUsunKategorie = itemView.findViewById(R.id.buttonUsunKategorie);
        }
    }
}

