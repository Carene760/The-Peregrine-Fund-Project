package com.example.theperegrinefund.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.theperegrinefund.HistoriqueMessageStatus;
import com.example.theperegrinefund.MyDatabaseHelper;
import com.example.theperegrinefund.StatusMessage;

import java.util.ArrayList;
import java.util.List;

public class HistoriqueMessageStatusDao {
    private MyDatabaseHelper dbHelper;

    public HistoriqueMessageStatusDao(Context context) {
        dbHelper = new MyDatabaseHelper(context);
    }

    public long insertHistorique(HistoriqueMessageStatus h) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_HISTORIQUE_ID, h.getIdHistorique());
        values.put(MyDatabaseHelper.COLUMN_HISTORIQUE_DATE, h.getDateChangement());
        values.put(MyDatabaseHelper.COLUMN_HISTORIQUE_STATUS_FK, h.getIdStatusMessage());
        values.put(MyDatabaseHelper.COLUMN_HISTORIQUE_MESSAGE_FK, h.getIdMessage());
        // Supprimé: values.put("id_status", h.getIdStatus());
        
        long id = db.insert(MyDatabaseHelper.TABLE_HISTORIQUE, null, values);
        db.close();
        return id;
    }

    public List<HistoriqueMessageStatus> getAllHistorique() {
        List<HistoriqueMessageStatus> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(MyDatabaseHelper.TABLE_HISTORIQUE, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                HistoriqueMessageStatus h = new HistoriqueMessageStatus();
                h.setIdHistorique(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_HISTORIQUE_ID)));
                h.setDateChangement(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_HISTORIQUE_DATE)));
                h.setIdStatusMessage(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_HISTORIQUE_STATUS_FK)));
                h.setIdMessage(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_HISTORIQUE_MESSAGE_FK)));
                list.add(h);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
        public int getLastStatusForMessage(int messageId) {
        int lastStatusId = -1;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(
                "SELECT " + MyDatabaseHelper.COLUMN_HISTORIQUE_STATUS_FK +
                " FROM " + MyDatabaseHelper.TABLE_HISTORIQUE +
                " WHERE " + MyDatabaseHelper.COLUMN_HISTORIQUE_MESSAGE_FK + " = ?" +
                " ORDER BY " + MyDatabaseHelper.COLUMN_HISTORIQUE_DATE + " DESC LIMIT 1",
                new String[]{String.valueOf(messageId)}
            );

            if (cursor.moveToFirst()) {
                lastStatusId = cursor.getInt(
                    cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_HISTORIQUE_STATUS_FK)
                );
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return lastStatusId;
    }
       
    public List<HistoriqueMessageStatus> getStatusHistoryForMessage(int messageId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<HistoriqueMessageStatus> historiques = new ArrayList<>();

        String query = "SELECT h." + MyDatabaseHelper.COLUMN_HISTORIQUE_ID + ", " +
                "h." + MyDatabaseHelper.COLUMN_HISTORIQUE_DATE + ", " +
                "h." + MyDatabaseHelper.COLUMN_HISTORIQUE_STATUS_FK + ", " +
                "h." + MyDatabaseHelper.COLUMN_HISTORIQUE_MESSAGE_FK + ", " +
                "s." + MyDatabaseHelper.COLUMN_STATUS_NAME +
                " FROM " + MyDatabaseHelper.TABLE_HISTORIQUE + " h" +
                " JOIN " + MyDatabaseHelper.TABLE_STATUS + " s" +
                " ON h." + MyDatabaseHelper.COLUMN_HISTORIQUE_STATUS_FK + " = s." + MyDatabaseHelper.COLUMN_STATUS_ID +
                " WHERE h." + MyDatabaseHelper.COLUMN_HISTORIQUE_MESSAGE_FK + " = ?" +
                " ORDER BY h." + MyDatabaseHelper.COLUMN_HISTORIQUE_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(messageId)});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                HistoriqueMessageStatus historique = new HistoriqueMessageStatus();
                historique.setIdHistorique(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_HISTORIQUE_ID)));
                historique.setDateChangement(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_HISTORIQUE_DATE)));
                historique.setIdStatusMessage(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_HISTORIQUE_STATUS_FK)));
                historique.setIdMessage(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_HISTORIQUE_MESSAGE_FK)));
                
                StatusMessage status = new StatusMessage();
                status.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_STATUS_NAME)));
                historique.setStatus(status);
                
                historiques.add(historique);
            }
            cursor.close();
        }
        db.close();
        return historiques;
    }
}