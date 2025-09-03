// W com.example.zarzdzanie_finansami.ui.Adaptery.DataKalendarzFragment.java
package com.example.zarzdzanie_finansami.ui.Adaptery;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentResultListener; // Import dla FragmentResultListener

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DataKalendarzFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static final String REQUEST_KEY_DATE_PICKER = "REQUEST_KEY_DATE_PICKER";
    public static final String RESULT_KEY_SELECTED_DATE_STRING = "RESULT_KEY_SELECTED_DATE_STRING";
    public static final String RESULT_KEY_SELECTED_YEAR = "RESULT_KEY_SELECTED_YEAR";
    public static final String RESULT_KEY_SELECTED_MONTH = "RESULT_KEY_SELECTED_MONTH"; // 0-11
    public static final String RESULT_KEY_SELECTED_DAY = "RESULT_KEY_SELECTED_DAY";

    // Opcjonalne: argumenty do ustawienia początkowej daty w kalendarzu
    private static final String ARG_INITIAL_YEAR = "initial_year";
    private static final String ARG_INITIAL_MONTH = "initial_month";
    private static final String ARG_INITIAL_DAY = "initial_day";

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    /**
     * Tworzy nową instancję DataKalendarzFragment.
     * @return Nowa instancja DataKalendarzFragment.
     */
    public static DataKalendarzFragment newInstance() {
        return new DataKalendarzFragment();
    }

    /**
     * Tworzy nową instancję DataKalendarzFragment z ustawioną datą początkową.
     * @param year Rok początkowy.
     * @param month Miesiąc początkowy (0-11).
     * @param day Dzień miesiąca początkowy.
     * @return Nowa instancja DataKalendarzFragment.
     */
    public static DataKalendarzFragment newInstance(int year, int month, int day) {
        DataKalendarzFragment fragment = new DataKalendarzFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INITIAL_YEAR, year);
        args.putInt(ARG_INITIAL_MONTH, month);
        args.putInt(ARG_INITIAL_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }




    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Sprawdź, czy przekazano argumenty z datą początkową
        Bundle arguments = getArguments();
        if (arguments != null) {
            year = arguments.getInt(ARG_INITIAL_YEAR, year);
            month = arguments.getInt(ARG_INITIAL_MONTH, month);
            day = arguments.getInt(ARG_INITIAL_DAY, day);
        }

        // Utwórz nowy DatePickerDialog
        // Możesz wybrać styl dialogu, np. Theme_AppCompat_Light_Dialog_Alert
        // android.R.style.Theme_Holo_Light_Dialog_NoActionBar, // Przykład innego stylu

        return new DatePickerDialog(requireContext(),
                // android.R.style.Theme_Holo_Light_Dialog_NoActionBar, // Przykład innego stylu
                this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // 'month' jest 0-indeksowany (0=Styczeń, 11=Grudzień)

        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(year, month, dayOfMonth);

        String selectedDateString = dateFormatter.format(selectedCalendar.getTime());

        // Przygotuj wynik do wysłania
        Bundle result = new Bundle();
        result.putString(RESULT_KEY_SELECTED_DATE_STRING, selectedDateString);
        result.putInt(RESULT_KEY_SELECTED_YEAR, year);
        result.putInt(RESULT_KEY_SELECTED_MONTH, month); // Przekazujemy 0-11
        result.putInt(RESULT_KEY_SELECTED_DAY, dayOfMonth);

        // Użyj Fragment Result API do wysłania wyniku
        getParentFragmentManager().setFragmentResult(REQUEST_KEY_DATE_PICKER, result);

        // Dialog zamknie się automatycznie
    }
}
