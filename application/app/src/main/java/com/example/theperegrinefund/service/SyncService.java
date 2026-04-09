package com.example.theperegrinefund.service;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import com.example.theperegrinefund.Message;
import com.example.theperegrinefund.dao.MessageDao;
import com.example.theperegrinefund.dao.InterventionDao;
import com.example.theperegrinefund.dao.StatusMessageDao;
import com.example.theperegrinefund.dao.HistoriqueMessageStatusDao;
import com.example.theperegrinefund.dao.EvenementDao;
import com.example.theperegrinefund.StatusMessage;
import com.example.theperegrinefund.Intervention;
import com.example.theperegrinefund.HistoriqueMessageStatus;
import com.example.theperegrinefund.Evenement;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;

import com.example.theperegrinefund.security.ConfigLoader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SyncService {
    private static final String TAG = "SyncService";
    private final OkHttpClient client;
    private final Gson gson;
    private final MessageDao messageDao;
    private final InterventionDao interventionDao;
    private final StatusMessageDao statusDao;
    private final HistoriqueMessageStatusDao historiqueDao;
    private final EvenementDao evenementDao;
    private final String BASE_URL;
    private final Context context;

    public SyncService(Context context) {
        this.context = context;

        client = new OkHttpClient();

        // Configuration de Gson avec support de LocalDateTime
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return LocalDateTime.parse(json.getAsString());
            }
        });
        gson = gsonBuilder.create();

        messageDao = new MessageDao(context);
        interventionDao = new InterventionDao(context);
        statusDao = new StatusMessageDao(context);
        historiqueDao = new HistoriqueMessageStatusDao(context);
        evenementDao = new EvenementDao(context);

        String url;
        try {
            url = ConfigLoader.getBackupServerUrl(context);
            Log.d(TAG, "URL chargée depuis config.properties : " + url);
        } catch (Exception e) {
            Log.e(TAG, "Impossible de charger l'URL du serveur, utilisation de fallback", e);
            url = "https://a19675263dca.ngrok-free.app";
        }
        BASE_URL = url;
        Log.d(TAG, "BASE_URL finale: " + BASE_URL);
    }

    public interface MessageCallback {
        void onComplete(List<Message> messages);
        void onError(Exception e);
    }

    public interface InterventionCallback {
        void onComplete(List<Intervention> interventions);
        void onError(Exception e);
    }

    public interface EvenementCallback {
        void onComplete(List<Evenement> evenements);
        void onError(Exception e);
    }

    public interface StatusCallback {
        void onComplete(List<StatusMessage> statusMessages);
        void onError(Exception e);
    }

    public interface HistoriqueCallback {
        void onComplete(List<HistoriqueMessageStatus> historiques);
        void onError(Exception e);
    }

    public void downloadStatus(StatusCallback callback) {
        String url = BASE_URL + "/status";
        Request request = buildJsonRequest(url);

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String json = readJsonOrThrow(response.body());
                    Type listType = new TypeToken<List<StatusMessage>>() {}.getType();
                    List<StatusMessage> statusMessages = gson.fromJson(json, listType);

                    for (StatusMessage status : statusMessages) {
                        statusDao.insertStatus(status);
                    }

                    if (callback != null) {
                        callback.onComplete(statusMessages);
                    }
                } else {
                    if (callback != null) callback.onError(new IOException("HTTP " + response.code()));
                }
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        }).start();
    }

    public void downloadMessages(int idUser, MessageCallback callback) {
        String url = BASE_URL + "/download/" + idUser;
        Request request = buildJsonRequest(url);

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String json = readJsonOrThrow(response.body());
                    Type listType = new TypeToken<List<Message>>() {}.getType();
                    List<Message> messages = gson.fromJson(json, listType);

                    int insertedCount = 0;
                    for (Message msg : messages) {
                        normalizeMessageEvenement(msg);
                        msg.setIdUserApp(idUser);
                        messageDao.insertMessage(msg);
                        insertedCount++;
                    }

                    if (callback != null) {
                        callback.onComplete(messages);
                    }
                } else {
                    if (callback != null) callback.onError(new IOException("HTTP " + response.code()));
                }
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        }).start();
    }

    private void normalizeMessageEvenement(Message message) {
        if (message == null) {
            return;
        }

        if (message.getIdEvenement() == null && message.getEvenement() != null) {
            message.setIdEvenement(message.getEvenement().getIdEvenement());
        }
    }

    public void downloadIntervention(InterventionCallback callback) {
        String url = BASE_URL + "/interventions";
        Request request = buildJsonRequest(url);

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String json = readJsonOrThrow(response.body());
                    Type listType = new TypeToken<List<Intervention>>() {}.getType();
                    List<Intervention> interventions = gson.fromJson(json, listType);

                    for (Intervention inter : interventions) {
                        interventionDao.insertIntervention(inter);
                    }

                    if (callback != null) {
                        callback.onComplete(interventions);
                    }
                } else {
                    if (callback != null) callback.onError(new IOException("HTTP " + response.code()));
                }
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        }).start();
    }

    public void downloadEvenements(EvenementCallback callback) {
        String url = BASE_URL + "/evenements";
        Request request = buildJsonRequest(url);

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String json = readJsonOrThrow(response.body());
                    Type listType = new TypeToken<List<Evenement>>() {}.getType();
                    List<Evenement> evenements = gson.fromJson(json, listType);

                    for (Evenement evenement : evenements) {
                        evenementDao.insertEvenement(evenement);
                    }

                    if (callback != null) {
                        callback.onComplete(evenements);
                    }
                } else {
                    if (callback != null) callback.onError(new IOException("HTTP " + response.code()));
                }
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        }).start();
    }

    public void downloadHistorique(int idUser, HistoriqueCallback callback) {
        String url = BASE_URL + "/historique/" + idUser;
        Request request = buildJsonRequest(url);

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String json = readJsonOrThrow(response.body());
                    Type listType = new TypeToken<List<HistoriqueMessageStatus>>() {}.getType();
                    List<HistoriqueMessageStatus> historiques = gson.fromJson(json, listType);

                    for (HistoriqueMessageStatus h : historiques) {
                        if (h.getStatus() != null) h.setIdStatusMessage(h.getStatus().getIdStatusMessage());
                        if (h.getMessage() != null) h.setIdMessage(h.getMessage().getIdMessage());
                        historiqueDao.insertHistorique(h);
                    }

                    if (callback != null) {
                        callback.onComplete(historiques);
                    }
                } else {
                    if (callback != null) callback.onError(new IOException("HTTP " + response.code()));
                }
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        }).start();
    }

    private Request buildJsonRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("ngrok-skip-browser-warning", "true")
                .build();
    }

    private String readJsonOrThrow(ResponseBody body) throws IOException {
        String raw = body.string();
        String trimmed = raw == null ? "" : raw.trim();

        // Prevent Gson parse errors when ngrok/intermediary returns HTML/text instead of JSON.
        if (!trimmed.startsWith("[") && !trimmed.startsWith("{")) {
            String preview = trimmed.length() > 160 ? trimmed.substring(0, 160) + "..." : trimmed;
            throw new IOException("Réponse serveur non JSON: " + preview);
        }
        return trimmed;
    }
}
