package com.example.theperegrinefund;

import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import com.example.theperegrinefund.security.ConfigLoader;
import com.example.theperegrinefund.security.CryptoUtils;
import com.example.theperegrinefund.network.NetworkUtils;
import com.example.theperegrinefund.dao.OutboxDao;

import com.example.theperegrinefund.dao.HistoriqueMessageStatusDao;
import com.example.theperegrinefund.HistoriqueMessageStatus;

import okhttp3.ResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Sends alerts / status-history updates to the server.
 *
 * Priority order (this directly implements "use the internet directly when
 * available, SMS gateway only as fallback"):
 *  1. Check connectivity (ConnectivityManager) before even attempting HTTP.
 *  2. If a network is available, POST directly to the server first.
 *  3. Only fall back to SMS if there is no connectivity, the HTTP call
 *     times out/fails, or the server does not return a genuine JSON
 *     response (e.g. an ngrok interstitial HTML page on HTTP 200).
 *  4. Every outcome (HTTP success, SMS fallback success, or full failure)
 *     is recorded in the local outbox so nothing is silently dropped; rows
 *     that fail entirely are retried later by SendRetryWorker.
 */
public class ServerSender {
    private static final String TAG = "ServerSender";
    private static final DateTimeFormatter MESSAGE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ApiService apiService;
    private final SmsSender smsSender;
    private final android.content.Context context;
    private final OutboxDao outboxDao;
    private String SECRET_KEY;

    public ServerSender(ApiService apiService, SmsSender smsSender, android.content.Context context) {
        this.apiService = apiService;
        this.smsSender = smsSender;
        this.context = context;
        this.outboxDao = new OutboxDao(context);
        try {
            SECRET_KEY = ConfigLoader.getSecretKey(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String formatHistorique(HistoriqueMessageStatus historique) {
        if (historique == null) return "";

        return "dateChangement=" + historique.getDateChangement() + "/"
            + "idMessage=" + historique.getIdMessage() + "/"
            + "idStatus=" + historique.getIdStatusMessage();
    }

    /** Result callback for callers that need to react to the outcome (e.g. re-show UI controls on failure). */
    public interface SendCallback {
        void onSent();
        void onQueuedForRetry();
    }

    public void sendHistory(HistoriqueMessageStatus historique) {
        sendHistory(historique, null);
    }

    public void sendHistory(HistoriqueMessageStatus historique, SendCallback callback) {
        String content = formatHistorique(historique);

        final String encryptedContent;
        try {
            CryptoUtils crypto = new CryptoUtils(content);
            encryptedContent = crypto.chiffrer(SECRET_KEY);
        } catch (Exception e) {
            Toast.makeText(context, "Erreur lors du chiffrement : " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        int idUserApp = AppData.getCurrentUserId(context);
        dispatch(OutboxDao.TYPE_HISTORIQUE, encryptedContent, idUserApp,
                () -> smsSender.sendHistory(historique), callback);
    }

    public void send(Message message, String status) {
        send(message, status, null);
    }

    public void send(Message message, String status, SendCallback callback) {
        String content = formatMessage(message, status);

        final String encryptedContent;
        try {
            encryptedContent = message.chiffrer(SECRET_KEY, content);
        } catch (Exception e) {
            Toast.makeText(context, "Erreur lors du chiffrement : " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        dispatch(OutboxDao.TYPE_ALERTE, encryptedContent, message.getIdUserApp(),
                () -> smsSender.send(message, status), callback);
    }

    /** Common send/fallback/persist flow shared by send() and sendHistory(). */
    private void dispatch(String type, String encryptedContent, int idUserApp, SmsAttempt smsAttempt, SendCallback callback) {
        String phoneNumber = null;
        try {
            phoneNumber = ConfigLoader.getFixedNumber(context);
        } catch (Exception ignored) {
            // handled below
        }
        final String phoneNumberForRequest = phoneNumber;

        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.i(TAG, "Pas de connexion internet détectée, envoi direct HTTP ignoré -> SMS.");
            fallbackToSms(type, encryptedContent, phoneNumberForRequest, idUserApp, smsAttempt, callback);
            return;
        }

        ApiService.DirectMessageRequest request =
                new ApiService.DirectMessageRequest(phoneNumberForRequest, encryptedContent);

        Call<ResponseBody> call = apiService.sendEncryptedMessage(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && isGenuineJsonResponse(response)) {
                    outboxDao.insert(type, encryptedContent, phoneNumberForRequest, idUserApp, OutboxDao.STATUS_SENT);
                    Toast.makeText(context, "Message envoyé au serveur!", Toast.LENGTH_SHORT).show();
                    if (callback != null) callback.onSent();
                } else {
                    Log.w(TAG, "Réponse HTTP non exploitable (code=" + response.code()
                            + "), bascule sur SMS.");
                    fallbackToSms(type, encryptedContent, phoneNumberForRequest, idUserApp, smsAttempt, callback);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.w(TAG, "Echec de l'envoi HTTP direct, bascule sur SMS.", t);
                fallbackToSms(type, encryptedContent, phoneNumberForRequest, idUserApp, smsAttempt, callback);
            }
        });
    }

    private void fallbackToSms(String type, String encryptedContent, String phoneNumber, int idUserApp, SmsAttempt smsAttempt, SendCallback callback) {
        Toast.makeText(context, "Impossible de joindre le serveur. Envoi par SMS...", Toast.LENGTH_SHORT).show();
        try {
            smsAttempt.run();
            outboxDao.insert(type, encryptedContent, phoneNumber, idUserApp, OutboxDao.STATUS_SENT);
            Toast.makeText(context, "Message envoyé par SMS!", Toast.LENGTH_SHORT).show();
            if (callback != null) callback.onSent();
        } catch (Exception e) {
            Log.e(TAG, "Echec de l'envoi SMS de secours, mise en file d'attente locale.", e);
            outboxDao.insert(type, encryptedContent, phoneNumber, idUserApp, OutboxDao.STATUS_PENDING);
            Toast.makeText(context, "Erreur SMS : " + e.getMessage() + ". Message mis en attente pour renvoi.", Toast.LENGTH_LONG).show();
            if (callback != null) callback.onQueuedForRetry();
        }
    }

    private boolean isGenuineJsonResponse(Response<ResponseBody> response) {
        try {
            ResponseBody body = response.body();
            if (body == null) {
                return false;
            }
            NetworkUtils.readJsonOrThrow(body);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Réponse serveur non JSON (probable page ngrok/proxy).", e);
            return false;
        }
    }

    private interface SmsAttempt {
        void run() throws Exception;
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
