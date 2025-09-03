package com.example.zarzdzanie_finansami.ui.Adaptery; // lub com.example.zarzdzanie_finansami.adapter

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zarzdzanie_finansami.R;
import com.example.zarzdzanie_finansami.dto.transakcja.TransakcjaOdpowiedz; // Upewnij się, że ta klasa istnieje i ma pola
import java.util.List;
import java.text.NumberFormat; // Do formatowania kwoty
import java.util.Locale; // Do formatowania kwoty

public class TransakcjeAdapter extends RecyclerView.Adapter<TransakcjeAdapter.TransactionViewHolder> {

    private List<TransakcjaOdpowiedz> transactionList;
    private OnTransactionListener onTransactionListener;
    private NumberFormat currencyFormatter;


    public interface OnTransactionListener {
        void onModifyClick(TransakcjaOdpowiedz transakcja);
        void onDeleteClick(int transakcjaId, int position);
    }

    public TransakcjeAdapter(List<TransakcjaOdpowiedz> transactionList, OnTransactionListener onTransactionListener) {
        this.transactionList = transactionList;
        this.onTransactionListener = onTransactionListener;
        // Inicjalizuj formatter waluty, możesz dostosować Locale
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pl", "PL"));
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_transakcje, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransakcjaOdpowiedz currentTransaction = transactionList.get(position);

        holder.description.setText(currentTransaction.getOpis()); // Założenie: TransakcjaResponse ma getOpis()

        // Założenie: TransakcjaResponse ma getKwota() zwracającą BigDecimal i getWaluta()
        if (currentTransaction.getKwota() != null) {
            String formattedAmount = currencyFormatter.format(currentTransaction.getKwota());
            holder.amount.setText("Kwota: " + formattedAmount);
        } else {
            holder.amount.setText("Kwota: Brak danych");
        }

        holder.date.setText("Data: " + currentTransaction.getData()); // Założenie: TransakcjaResponse ma getData()
        holder.type.setText("Typ: " + currentTransaction.getTyp());   // Założenie: TransakcjaResponse ma getTyp()


        holder.buttonModify.setOnClickListener(v ->
                onTransactionListener.onModifyClick(currentTransaction));

        holder.buttonDelete.setOnClickListener(v ->
                onTransactionListener.onDeleteClick(currentTransaction.getId(), holder.getAdapterPosition())); // Założenie: TransakcjaResponse ma getId()
    }

    @Override
    public int getItemCount() {
        return transactionList == null ? 0 : transactionList.size();
    }

    public void setTransactions(List<TransakcjaOdpowiedz> transactions) {
        this.transactionList = transactions;
        notifyDataSetChanged(); // Lub użyj DiffUtil dla lepszej wydajności
    }

    public void removeTransactionAt(int position) {
        if (position >= 0 && position < transactionList.size()) {
            transactionList.remove(position);
            notifyItemRemoved(position);
        }
    }


    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView description, amount, date, type;
        Button buttonModify, buttonDelete;

        TransactionViewHolder(View view) {
            super(view);
            description = view.findViewById(R.id.textViewTransactionDescription);
            amount = view.findViewById(R.id.textViewTransactionAmount);
            date = view.findViewById(R.id.textViewTransactionDate);
            type = view.findViewById(R.id.textViewTransactionType);
            buttonModify = view.findViewById(R.id.buttonModifyTransaction);
            buttonDelete = view.findViewById(R.id.buttonDeleteTransaction);
        }
    }
}
