package com.example.zarzdzanie_finansami.network; // Lub inny odpowiedni pakiet

import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault());
    // Użyj tego samego formatu, co backend w @JsonFormat

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(formatter.format(value));
        }
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String dateStr = in.nextString();
        try {
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            // Możesz tu zalogować błąd lub rzucić IOException, jeśli preferujesz ścisłą obsługę
            Log.e("LocalDateTypeAdapter", "Nie udało się sparsować daty: " + dateStr, e);
            return null; // Lub rzuć wyjątek, jeśli data musi zawsze być poprawna
        }
    }
}

