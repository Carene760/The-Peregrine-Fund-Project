package com.example.theperegrinefund;

import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.example.theperegrinefund.security.ConfigLoader;
import com.example.theperegrinefund.security.CryptoUtils;

import com.example.theperegrinefund.dao.HistoriqueMessageStatusDao;
import com.example.theperegrinefund.HistoriqueMessageStatus;


import java.util.Date;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerSender {
    private static final DateTimeFormatter MESSAGE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ApiService apiService;
    private final SmsSender smsSender;
    private final Context context;
    private String SECRET_KEY;

    public ServerSender(ApiService apiService, SmsSender smsSender, Context context) {
        this.apiService = apiService;
        this.smsSender = smsSender;
        this.context = context;
        try {
            SECRET_KEY = ConfigLoader.getSecretKey(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String formatHistorique(HistoriqueMessageStatus historique) {
        HistoriqueMessageStatusDao historiqueDao = new HistoriqueMessageStatusDao(context);
      
        if (historique == null) return "";
        
        return "dateChangement=" + historique.getDateChangement() + "/"
            + "idMessage=" + historique.getIdMessage() + "/"
            + "idStatus=" + historique.getIdStatusMessage();
    }
    
    public void sendHistory(HistoriqueMessageStatus historique) {
            String content = formatHistorique(historique);
            HistoriqueMessageStatusDao historiqueDao = new HistoriqueMessageStatusDao(context);

            final String encryptedContent;
            try {
                CryptoUtils crypto = new CryptoUtils(content);
                encryptedContent = crypto.chiffrer(SECRET_KEY);
            } catch (Exception e) {
                Toast.makeText(context, "Erreur lors du chiffrement : " + e.getMessage(), Toast.LENGTH_LONG).show();
                return; // on stoppe ici si le chiffrement échoue

            }

            Call<Void> call = apiService.sendEncryptedMessage(encryptedContent);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                    //   Long newId = historiqueDao.insertHistorique(historique);
                        // message.setIdMessage(newId);
                        // MyDatabaseHelper dbHelper = new MyDatabaseHelper(context);
                        // int idStatus = dbHelper.getIdStatusMessage(status);
                        // StatusMessage statusMessage = new StatusMessage(idStatus, status);
                        // HistoriqueMessageStatus historique = new HistoriqueMessageStatus(new Date(), statusMessage, message);
                        // historique.save(context);
                        Toast.makeText(context, "Message envoyé au serveur!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Erreur serveur", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(context, "Impossible de joindre le serveur. Historique sauvegardé localement.", Toast.LENGTH_SHORT).show();
                    // Save historique locally when server is unreachable
                // historiqueDao.insertHistorique(historique);
                }
            });
        
    }

    public void send(Message message, String status) {
        String content = formatMessage(message, status);

        final String encryptedContent;
        try {
            encryptedContent = message.chiffrer(SECRET_KEY, content);
        } catch (Exception e) {
            Toast.makeText(context, "Erreur lors du chiffrement : " + e.getMessage(), Toast.LENGTH_LONG).show();
            return; // on stoppe ici si le chiffrement échoue
        }

        Call<Void> call = apiService.sendEncryptedMessage(encryptedContent);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
               //     int newId = message.save(context);
                    // message.setIdMessage(newId);
                    // MyDatabaseHelper dbHelper = new MyDatabaseHelper(context);
                    // int idStatus = dbHelper.getIdStatusMessage(status);
                    // StatusMessage statusMessage = new StatusMessage(idStatus, status);
                    // HistoriqueMessageStatus historique = new HistoriqueMessageStatus(new Date(), statusMessage, message);
                    // historique.save(context);
                    Toast.makeText(context, "Message envoyé au serveur!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Impossible de joindre le serveur. Envoi par SMS...", Toast.LENGTH_SHORT).show();
                try {
                    smsSender.send(message, status);
                    Toast.makeText(context, "Message envoyé par SMS!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(context, "Erreur SMS : " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
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
}
