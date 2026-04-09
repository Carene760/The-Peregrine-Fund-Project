package com.example.theperegrinefund;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import com.google.android.material.textfield.TextInputEditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Fragment2 extends Fragment {

    private TextInputEditText editDateSignalement;
    private EditText editPointRepere;
    private EditText editDescription;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflater le layout du fragment
        View view = inflater.inflate(R.layout.fragment_page2, container, false);

        editDateSignalement = view.findViewById(R.id.edit_datetime_signalement);
        if (editDateSignalement != null) {
            editDateSignalement.setOnClickListener(v -> openDateTimePicker(editDateSignalement));
        }


        // Point de repère (texte long)
        editPointRepere = view.findViewById(R.id.edit_pointR);
        editPointRepere.setSingleLine(false);
        editPointRepere.setLines(3); // hauteur initiale
        editPointRepere.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);

        // Description (texte long)
        editDescription = view.findViewById(R.id.edit_desc);
        editDescription.setSingleLine(false);
        editDescription.setLines(4);
        editDescription.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);

        return view;
    }
    public String getPointRepere() {
        return editPointRepere != null ? editPointRepere.getText().toString() : "";
    }

    public String getDescription() {
        return editDescription != null ? editDescription.getText().toString() : "";
    }

    public LocalDateTime getDateSignalement() {
        if (editDateSignalement != null && !editDateSignalement.getText().toString().isEmpty()) {
            String value = editDateSignalement.getText().toString();
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void openDateTimePicker(TextInputEditText editText) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        android.app.DatePickerDialog datePicker = new android.app.DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    android.app.TimePickerDialog timePicker = new android.app.TimePickerDialog(getContext(),
                            (timeView, hourOfDay, minute) -> {
                                String dateTime = String.format("%02d/%02d/%04d %02d:%02d",
                                        dayOfMonth, (month + 1), year, hourOfDay, minute);
                                editText.setText(dateTime);
                            },
                            calendar.get(java.util.Calendar.HOUR_OF_DAY),
                            calendar.get(java.util.Calendar.MINUTE),
                            true
                    );
                    timePicker.show();
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }
}
