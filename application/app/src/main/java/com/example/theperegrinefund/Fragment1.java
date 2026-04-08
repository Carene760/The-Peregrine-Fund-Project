package com.example.theperegrinefund;

import android.os.Bundle;
import android.app.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import java.util.Calendar;
import android.widget.AutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class Fragment1 extends Fragment {
    private EditText editSurface;
    private AutoCompleteTextView statusDropdown;
    private TextInputEditText editDateTime;
    private AutoCompleteTextView interventionDropdown;
    private AutoCompleteTextView zoneDropdown;
    private AutoCompleteTextView directionDropdown;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflater le layout du fragment
        View view = inflater.inflate(R.layout.fragment_page1, container, false);

        // Dropdown Status
        statusDropdown = view.findViewById(R.id.edit_status);
        String[] statuses = getResources().getStringArray(R.array.status_options);
        statusDropdown.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, statuses));
        statusDropdown.setOnClickListener(v -> statusDropdown.showDropDown());

        // DateTime picker
        editDateTime = view.findViewById(R.id.edit_datetime);
        if (editDateTime != null) {
            editDateTime.setOnClickListener(v -> openDateTimePicker(editDateTime));
        }

        // Dropdown Intervention
        interventionDropdown = view.findViewById(R.id.edit_intervention);
        String[] interventions = getResources().getStringArray(R.array.intervention_options);
        interventionDropdown.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, interventions));
        interventionDropdown.setOnClickListener(v -> interventionDropdown.showDropDown());

        // Dropdown Zone
        zoneDropdown = view.findViewById(R.id.edit_zone);
        String[] zones = getResources().getStringArray(R.array.zone_options);
        zoneDropdown.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, zones));
        zoneDropdown.setOnClickListener(v -> zoneDropdown.showDropDown());

        
        // Dropdown direction
        directionDropdown = view.findViewById(R.id.edit_direction);
        String[] directions = getResources().getStringArray(R.array.direction_options);
        directionDropdown.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, directions));
        directionDropdown.setOnClickListener(v -> directionDropdown.showDropDown());

        // Surface (double)
        editSurface = view.findViewById(R.id.edit_surface);
        editSurface.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                                 android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        return view;
    }

    public double getSurface() {
        if (editSurface != null && !editSurface.getText().toString().isEmpty()) {
            try {
                return Double.parseDouble(editSurface.getText().toString());
            } catch (NumberFormatException e) {
                e.printStackTrace(); 
                return 0.0; // valeur par défaut si erreur
            }
        }
        return 0.0; // si editSurface est null ou vide
    }


    public String getStatus() 
    {
        return statusDropdown != null ? statusDropdown.getText().toString() : "";
    }

   public LocalDateTime getDateTime() {
        if (editDateTime != null && !editDateTime.getText().toString().isEmpty()) {
            String value = editDateTime.getText().toString();
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public Intervention getIntervention() { 
        String selectedText = interventionDropdown != null ? interventionDropdown.getText().toString() : ""; 

        MyDatabaseHelper dbHelper = new MyDatabaseHelper(requireContext());
        int idIntervention = dbHelper.getIdIntervention(selectedText);
        Intervention interventionObj = new Intervention(idIntervention, selectedText); 
        return interventionObj; 
    }

    public boolean isRenfort() 
    {
        return zoneDropdown != null ? zoneDropdown.getText().toString().equals("Oui") : false;
    }

    public String getDirection() 
    {
        return directionDropdown != null ? directionDropdown.getText().toString() : "";
    }
    // Méthode locale pour le DateTime picker
    private void openDateTimePicker(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                            (timeView, hourOfDay, minute) -> {
                                String dateTime = String.format("%02d/%02d/%04d %02d:%02d",
                                        dayOfMonth, (month + 1), year, hourOfDay, minute);
                                editText.setText(dateTime);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }
}
