package com.example.theperegrinefund;

import java.net.ContentHandler;

import android.telephony.SmsManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.util.Date;
import com.example.theperegrinefund.security.ConfigLoader;
import android.util.Log;
import java.util.ArrayList;
import com.example.theperegrinefund.security.CryptoUtils;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class SmsSender {
    private static final DateTimeFormatter MESSAGE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private Context context;  
    private String SECRET_KEY;
    private String num;

    public SmsSender(Context context) {
        this.context = context;
        try {
            SECRET_KEY = ConfigLoader.getSecretKey(context);
            num =   ConfigLoader.getFixedNumber(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

  public void send(Message message, String status) throws Exception {
    try {
        if (!message.isValid()) {
            throw new Exception("Numéro invalide: " + message.getPhoneNumber());
        }
        
        String content = formatMessage(message, status);
        Log.d("SMS", "Message original: " + content);
        
        String encryptedContent = message.chiffrer(SECRET_KEY, content);
        Log.d("SMS", "Message chiffré: " + encryptedContent);

        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> parts = smsManager.divideMessage(encryptedContent);
        
        if(parts.size() > 1) {
            smsManager.sendMultipartTextMessage(
                message.getPhoneNumber(),
                null,
                parts,
                null,
                null
            );
        } else {
            smsManager.sendTextMessage(
                message.getPhoneNumber(),
                null,
                encryptedContent,
                null,
                null
            );
        }
        
       // int newId = message.save(context);
      //  long a = historique.insertHistorique
       // Log.d("SMS", "Message sauvegardé avec ID: " + newId);
        
    } catch (Exception e) {
        Log.e("SMS", "Erreur envoi: " + e.getMessage(), e);
        throw e;
    }
}
    public void sendUser(String message) throws Exception {
        if (message == null || message.trim().isEmpty()) {
            throw new Exception("Contenu invalide");
        }
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(
                num,
                null,
                message,
                null,
                null
        );
    }
    public String formatMessage(Message message, String status) {
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(context);
        int idStatus = dbHelper.getIdStatusMessage(status);
        if (message == null) return "";

        LocalDateTime dateEnvoi = message.getDateEnvoi();
        if (dateEnvoi == null) {
            dateEnvoi = LocalDateTime.now();
            message.setDateEnvoi(dateEnvoi);
        }

        return "dateCommencement=" + formatDate(message.getDateCommencement()) + "/"
                + "dateSignalement=" + formatDate(message.getDateSignalement()) + "/"
                + "idIntervention=" + message.getIdIntervention() + "/"
                + "renfort=" + message.isRenfort() + "/"
                + "direction=" + encodeValue(message.getDirection()) + "/"
                + "surfaceApproximative=" + message.getSurfaceApproximative() + "/"
                + "pointRepere=" + encodeValue(message.getPointRepere()) + "/"
                + "description=" + encodeValue(message.getDescription()) + "/"
                + "idUserApp=" + message.getIdUserApp() + "/"
                + "longitude=" + message.getLongitude() + "/"
                + "latitude=" + message.getLatitude() + "/"
                + "idStatus=" + idStatus + "/"
                + "dateEnvoi=" + formatDate(dateEnvoi);
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) return "";
        return date.format(MESSAGE_DATE_FORMAT);
    }

    private String encodeValue(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    public void sendHistory(HistoriqueMessageStatus historique) throws Exception {
        if (historique == null) {
            throw new Exception("Historique vide");
        }

        String content = formatHistorique(historique);
        Log.d("SMS", "Historique original: " + content);

        CryptoUtils crypto = new CryptoUtils(content);
        String encryptedContent = crypto.chiffrer(SECRET_KEY);

        Log.d("SMS", "Historique chiffré: " + encryptedContent);

        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> parts = smsManager.divideMessage(encryptedContent);

        if (parts.size() > 1) {
            smsManager.sendMultipartTextMessage(
                    num,
                    null,
                    parts,
                    null,
                    null
            );
        } else {
            smsManager.sendTextMessage(
                    num,
                    null,
                    encryptedContent,
                    null,
                    null
            );
        }
    }

    public String formatHistorique(HistoriqueMessageStatus historique) {
    if (historique == null) return "";

        return "dateChangement=" + historique.getDateChangement() + "/"
            + "idMessage=" + historique.getIdMessage() + "/"
            + "idStatus=" + historique.getIdStatusMessage();
}


}
