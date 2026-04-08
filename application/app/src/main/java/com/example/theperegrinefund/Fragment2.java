package com.example.theperegrinefund;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Fragment2 extends Fragment {

    private EditText editPointRepere;
    private EditText editDescription;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflater le layout du fragment
        View view = inflater.inflate(R.layout.fragment_page2, container, false);


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
}
