package com.example.theperegrinefund.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.theperegrinefund.StatusMessage;
import com.example.theperegrinefund.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class StatusMessageDao {
    private MyDatabaseHelper dbHelper;

    public StatusMessageDao(Context context) {
        dbHelper = new MyDatabaseHelper(context);
    }

    public long insertStatus(StatusMessage status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idStatusMessage", status.getIdStatusMessage()); // ID du serveur
        values.put("status", status.getStatus());
        
        long id = db.insertWithOnConflict(
                "status_message",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
        db.close();
        return id;
    }

    public List<StatusMessage> getAllStatus() {
        List<StatusMessage> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("status_message", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                // Utilisation du constructeur avec paramètres
                StatusMessage status = new StatusMessage(
                    cursor.getInt(cursor.getColumnIndexOrThrow("idStatusMessage")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status"))
                );
                list.add(status);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
    public boolean exists(int idStatusMessage) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor cursor = db.query(
            MyDatabaseHelper.TABLE_STATUS,                // nom de la table
            new String[]{MyDatabaseHelper.COLUMN_STATUS_ID}, // colonne à vérifier
            MyDatabaseHelper.COLUMN_STATUS_ID + " = ?",   // clause WHERE
            new String[]{String.valueOf(idStatusMessage)},// valeur du paramètre
            null, null, null
    );

    boolean exists = (cursor.getCount() > 0);
    cursor.close();
    return exists;
}
 public String getStatusNameId(int statusId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String statusName = "Inconnu";
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(
                "SELECT " + MyDatabaseHelper.COLUMN_STATUS_NAME + 
                " FROM " + MyDatabaseHelper.TABLE_STATUS +
                " WHERE " + MyDatabaseHelper.COLUMN_STATUS_ID + " = ?",
                new String[]{String.valueOf(statusId)}
            );

            if (cursor.moveToFirst()) {
                statusName = cursor.getString(
                    cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_STATUS_NAME)
                );
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return statusName;
    }
  

}