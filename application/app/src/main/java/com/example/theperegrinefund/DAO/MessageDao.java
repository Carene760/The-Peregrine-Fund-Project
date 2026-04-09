package com.example.theperegrinefund.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.theperegrinefund.MyDatabaseHelper;
import com.example.theperegrinefund.Message;
import com.example.theperegrinefund.Evenement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MessageDao {
    private final MyDatabaseHelper dbHelper;
    // private final SimpleDateFormat dateFormat;
    private final DateTimeFormatter dateTimeFormatter;
    
    public MessageDao(Context context) {
        dbHelper = new MyDatabaseHelper(context);
        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
}

    public long insertMessage(Message message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(MyDatabaseHelper.COLUMN_MESSAGE_ID, message.getIdMessage());
        // Conversion des Date en String pour le stockage en base
        if (message.getDateCommencement() != null) {
           values.put(MyDatabaseHelper.COLUMN_DATE_COMMENCEMENT, message.getDateCommencement().format(dateTimeFormatter));
        }
        if (message.getDateSignalement() != null) {
              values.put(MyDatabaseHelper.COLUMN_DATE_SIGNAL, message.getDateSignalement().format(dateTimeFormatter));
        }
          if (message.getDateEnvoi() != null) {
              values.put(MyDatabaseHelper.COLUMN_DATE_ENVOI, message.getDateEnvoi().format(dateTimeFormatter));
          } else {
              values.put(MyDatabaseHelper.COLUMN_DATE_ENVOI, LocalDateTime.now().format(dateTimeFormatter));
          }
        values.put(MyDatabaseHelper.COLUMN_POINT_REPERE, message.getPointRepere());
        values.put(MyDatabaseHelper.COLUMN_SURFACE, message.getSurfaceApproximative());
        values.put(MyDatabaseHelper.COLUMN_DESCRIPTION, message.getDescription());
        values.put(MyDatabaseHelper.COLUMN_DIRECTION, message.getDirection());
        values.put(MyDatabaseHelper.COLUMN_RENFORT, message.isRenfort() ? 1 : 0); // Convertir boolean to int
        values.put(MyDatabaseHelper.COLUMN_LONGITUDE, message.getLongitude());
        values.put(MyDatabaseHelper.COLUMN_LATITUDE, message.getLatitude());
        values.put(MyDatabaseHelper.COLUMN_INTERVENTION_FK, message.getIdIntervention());
        values.put(MyDatabaseHelper.COLUMN_USER_FK, message.getIdUserApp());
        values.put(MyDatabaseHelper.COLUMN_EVENEMENT_FK, message.getIdEvenement());
        values.put(MyDatabaseHelper.COLUMN_PHONE_NUMBER, message.getPhoneNumber());
       

        return db.insert(MyDatabaseHelper.TABLE_MESSAGE, null, values);
    }
    public ArrayList<Message> getAllMessagesArrayList() {
    ArrayList<Message> messages = new ArrayList<>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    Cursor cursor = db.query(
            MyDatabaseHelper.TABLE_MESSAGE,
            null, null, null, null, null, null
    );

    while (cursor.moveToNext()) {
        Message msg = new Message();
        msg.setIdMessage(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_MESSAGE_ID)));

        // Conversion String → Date
        String commencementStr = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DATE_COMMENCEMENT));
        if (commencementStr != null) {
            try {
                msg.setDateCommencement(LocalDateTime.parse(commencementStr, dateTimeFormatter));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String signalementStr = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DATE_SIGNAL));
        if (signalementStr != null) {
            try {
                msg.setDateSignalement(LocalDateTime.parse(signalementStr, dateTimeFormatter));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String envoiStr = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DATE_ENVOI));
        if (envoiStr != null) {
            try {
                msg.setDateEnvoi(LocalDateTime.parse(envoiStr, dateTimeFormatter));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        msg.setPointRepere(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_POINT_REPERE)));
        msg.setSurfaceApproximative(cursor.getDouble(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_SURFACE)));
        msg.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DESCRIPTION)));
        msg.setDirection(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DIRECTION)));
        msg.setRenfort(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_RENFORT)) == 1);
        msg.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_LONGITUDE)));
        msg.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_LATITUDE)));
        msg.setIdIntervention(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_INTERVENTION_FK)));
        msg.setIdUserApp(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_USER_FK)));
        msg.setIdEvenement(cursor.isNull(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_FK)) ? null : cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_FK)));
        msg.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_PHONE_NUMBER)));

        messages.add(msg);
    }

    cursor.close();
    db.close();
    return messages;
}

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MyDatabaseHelper.TABLE_MESSAGE,
                null, null, null, null, null, null
        );

        while (cursor.moveToNext()) {
            Message msg = new Message();
            msg.setIdMessage(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_MESSAGE_ID)));
              // Conversion des String en Date
            String commencementStr = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DATE_COMMENCEMENT));
            if (commencementStr != null) {
                try {
                    msg.setDateCommencement(LocalDateTime.parse(commencementStr, dateTimeFormatter));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            String signalementStr = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DATE_SIGNAL));
            if (signalementStr != null) {
                try {
                    msg.setDateSignalement(LocalDateTime.parse(signalementStr, dateTimeFormatter));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String envoiStr = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DATE_ENVOI));
            if (envoiStr != null) {
                try {
                    msg.setDateEnvoi(LocalDateTime.parse(envoiStr, dateTimeFormatter));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            msg.setPointRepere(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_POINT_REPERE)));
            msg.setSurfaceApproximative(cursor.getDouble(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_SURFACE)));
            msg.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DESCRIPTION)));
            msg.setDirection(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DIRECTION)));
            
            // Conversion de int (0/1) vers boolean pour le renfort
            msg.setRenfort(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_RENFORT)) == 1);
            
            msg.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_LONGITUDE)));
            msg.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_LATITUDE)));
            msg.setIdIntervention(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_INTERVENTION_FK)));
            msg.setIdUserApp(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_USER_FK)));
            msg.setIdEvenement(cursor.isNull(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_FK)) ? null : cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_FK)));
            msg.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_PHONE_NUMBER)));
        

            messages.add(msg);
        }

        cursor.close();
        db.close();
        return messages;
    }
    public Message getMessageById(int messageId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase(); // Correction ici
        Message message = null;
        
        Cursor cursor = db.query(MyDatabaseHelper.TABLE_MESSAGE, // Correction ici
                null, 
                MyDatabaseHelper.COLUMN_MESSAGE_ID + " = ?", // Correction ici
                new String[]{String.valueOf(messageId)}, 
                null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            message = new Message();
            message.setIdMessage(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_MESSAGE_ID))); // Correction
            // Conversion des String en Date
            String commencementStr = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DATE_COMMENCEMENT));
            if (commencementStr != null) {
                try {
                    message.setDateCommencement(LocalDateTime.parse(commencementStr, dateTimeFormatter));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            String signalementStr = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DATE_SIGNAL));
            if (signalementStr != null) {
                try {
                    message.setDateSignalement(LocalDateTime.parse(signalementStr, dateTimeFormatter));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String envoiStr = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DATE_ENVOI));
            if (envoiStr != null) {
                try {
                    message.setDateEnvoi(LocalDateTime.parse(envoiStr, dateTimeFormatter));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            message.setPointRepere(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_POINT_REPERE))); // Correction
            message.setSurfaceApproximative(cursor.getDouble(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_SURFACE))); // Correction
            message.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DESCRIPTION))); // Correction
            message.setDirection(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_DIRECTION))); // Correction
            message.setRenfort(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_RENFORT)) == 1); // Correction
            message.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_LONGITUDE))); // Correction
            message.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_LATITUDE))); // Correction
            message.setIdIntervention(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_INTERVENTION_FK))); // Correction
            message.setIdUserApp(cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_USER_FK))); // Correction
            message.setIdEvenement(cursor.isNull(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_FK)) ? null : cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_FK)));
            message.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_PHONE_NUMBER))); // Correction

            if (message.getIdEvenement() != null) {
                Cursor eventCursor = db.query(
                        MyDatabaseHelper.TABLE_EVENEMENT,
                        null,
                        MyDatabaseHelper.COLUMN_EVENEMENT_ID + " = ?",
                        new String[]{String.valueOf(message.getIdEvenement())},
                        null,
                        null,
                        null
                );

                if (eventCursor != null) {
                    if (eventCursor.moveToFirst()) {
                        Evenement evenement = new Evenement();
                        evenement.setIdEvenement(eventCursor.getInt(eventCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_ID)));
                        evenement.setNom(eventCursor.getString(eventCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_NOM)));
                        evenement.setDate(eventCursor.getString(eventCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_DATE)));
                        evenement.setDescription(eventCursor.getString(eventCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EVENEMENT_DESCRIPTION)));
                        message.setEvenement(evenement);
                    }
                    eventCursor.close();
                }
            }
            cursor.close();
        }
        db.close();
        return message;
    }
        
    public boolean exists(int idMessage) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                MyDatabaseHelper.TABLE_MESSAGE,                // nom de la table
                new String[]{MyDatabaseHelper.COLUMN_MESSAGE_ID}, // colonne à vérifier
                MyDatabaseHelper.COLUMN_MESSAGE_ID + " = ?",   // clause WHERE
                new String[]{String.valueOf(idMessage)},       // valeur du paramètre
                null, null, null
        );

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }
    
   
}
