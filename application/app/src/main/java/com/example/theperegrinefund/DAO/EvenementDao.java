package com.example.theperegrinefund.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.theperegrinefund.Evenement;
import com.example.theperegrinefund.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class EvenementDao {
    private final MyDatabaseHelper dbHelper;

    public EvenementDao(Context context) {
        dbHelper = new MyDatabaseHelper(context);
    }

    public long insertEvenement(Evenement evenement) {
        if (evenement == null || evenement.getIdEvenement() == null) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_EVENEMENT_ID, evenement.getIdEvenement());
        values.put(MyDatabaseHelper.COLUMN_EVENEMENT_NOM, evenement.getNom());
        values.put(MyDatabaseHelper.COLUMN_EVENEMENT_DATE, evenement.getDate());
        values.put(MyDatabaseHelper.COLUMN_EVENEMENT_DESCRIPTION, evenement.getDescription());

        long id = db.insertWithOnConflict(
                MyDatabaseHelper.TABLE_EVENEMENT,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        db.close();
        return id;
    }

    public Evenement getEvenementById(Integer idEvenement) {
        if (idEvenement == null) {
            return null;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                MyDatabaseHelper.TABLE_EVENEMENT,
                null,
                MyDatabaseHelper.COLUMN_EVENEMENT_ID + " = ?",
                new String[]{String.valueOf(idEvenement)},
                null,
                null,
                null
        );

        Evenement evenement = null;
        if (cursor != null && cursor.moveToFirst()) {
            evenement = new Evenement();
            evenement.setIdEvenement(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_ID)));
            evenement.setNom(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_NOM)));
            evenement.setDate(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_DATE)));
            evenement.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_DESCRIPTION)));
            cursor.close();
        }

        db.close();
        return evenement;
    }

    public List<Evenement> getAllEvenements() {
        List<Evenement> evenements = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                MyDatabaseHelper.TABLE_EVENEMENT,
                null,
                null,
                null,
                null,
                null,
                MyDatabaseHelper.COLUMN_EVENEMENT_DATE + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Evenement evenement = new Evenement();
                evenement.setIdEvenement(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_ID)));
                evenement.setNom(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_NOM)));
                evenement.setDate(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_DATE)));
                evenement.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_DESCRIPTION)));
                evenements.add(evenement);
            }
            cursor.close();
        }

        db.close();
        return evenements;
    }
}
