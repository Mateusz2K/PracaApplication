package com.example.zarzdzanie_finansami.ui.Adaptery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.dto.budzet.BudzetOdpowiedz;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BudzetAdapter extends RecyclerView.Adapter<BudzetAdapter.BudzetViewHolder> {

    private List<BudzetOdpowiedz> budzetList;
    private final OnBudzetInteractionListener listener;
    private final Context context;
    private final NumberFormat currencyFormatter;

    public interface OnBudzetInteractionListener {
        void onBudzetClicked(BudzetOdpowiedz budzet);

        void onBudzetLongClicked(BudzetOdpowiedz budzet);

        void onManageBudgetItemsClicked(BudzetOdpowiedz budzet);
        void onDeleteBudzetClicked(BudzetOdpowiedz budzet);
    }

    public BudzetAdapter(Context context, List<BudzetOdpowiedz> budzetList, OnBudzetInteractionListener listener) {
        this.context = context;
        this.budzetList = budzetList;
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pl", "PL"));
    }

    @NonNull
    @Override
    public BudzetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budzet, parent, false);
        return new BudzetViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BudzetViewHolder holder, int position) {
        BudzetOdpowiedz currentBudzet = budzetList.get(position);
        holder.bind(currentBudzet, context, currencyFormatter);

        holder.optionsButton.setOnClickListener(v -> showPopupMenu(holder.optionsButton, currentBudzet));
    }

    private void showPopupMenu(View view, BudzetOdpowiedz budzet) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.menu_budzet_options);
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_manage_budget_items) {
                listener.onManageBudgetItemsClicked(budzet);
                return true;
            } else if (itemId == R.id.action_delete_budget) {
                listener.onDeleteBudzetClicked(budzet);
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return budzetList == null ? 0 : budzetList.size();
    }

    public void setBudzety(List<BudzetOdpowiedz> budzety) {
        this.budzetList = budzety;
        notifyDataSetChanged();
    }

    static class BudzetViewHolder extends RecyclerView.ViewHolder {
        TextView nazwaBudzetu, dataBudzetu, kwotaBudzetu, saldoBudzetu;
        ImageButton optionsButton;

        BudzetViewHolder(View view) {
            super(view);
            nazwaBudzetu = view.findViewById(R.id.nazwaBudzetu);
            dataBudzetu = view.findViewById(R.id.dataBudzetu);
            kwotaBudzetu = view.findViewById(R.id.kwotaBudzetu);
            saldoBudzetu = view.findViewById(R.id.saldoBudzetu);
            optionsButton = view.findViewById(R.id.buttonBudzetOptions);
        }

        public void bind(final BudzetOdpowiedz budzet, Context context, NumberFormat formatter) {
            nazwaBudzetu.setText(budzet.getNazwa());
            dataBudzetu.setText(String.format("Od: %s Do: %s", budzet.getDataPoczatkowa(), budzet.getDataKoncowa()));

            if (budzet.getPrzewidywanyDochod() != null) {
                kwotaBudzetu.setText("Przewidywany dochód: " + formatter.format(budzet.getPrzewidywanyDochod()));
            } else {
                kwotaBudzetu.setText("Przewidywany dochód: -");
            }

            if (budzet.getSaldoBudzetu() != null) {
                saldoBudzetu.setText("Saldo: " + formatter.format(budzet.getSaldoBudzetu()));
                if (budzet.getSaldoBudzetu().doubleValue() >= 0) {
                    saldoBudzetu.setTextColor(ContextCompat.getColor(context, R.color.green_profit));
                } else {
                    saldoBudzetu.setTextColor(ContextCompat.getColor(context, R.color.red_loss));
                }
            } else {
                saldoBudzetu.setText("Saldo: -");
            }
        }
    }
}