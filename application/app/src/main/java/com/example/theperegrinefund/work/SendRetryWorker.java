package com.example.theperegrinefund.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.theperegrinefund.ApiService;
import com.example.theperegrinefund.dao.OutboxDao;
import com.example.theperegrinefund.network.NetworkClientProvider;
import com.example.theperegrinefund.network.NetworkUtils;
import com.example.theperegrinefund.security.ConfigLoader;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Basic retry worker for the local outbox: on every run (periodic, or
 * triggered once connectivity is regained thanks to the NetworkType.CONNECTED
 * constraint) it retries every PENDING row via direct HTTP.
 *
 * Deliberately simple (first version of a retry/queue, not a full sync
 * engine): HTTP-only retry (no SMS re-send from here, to avoid duplicate SMS
 * costs/spam if the row was actually delivered but the app could not
 * confirm it), synchronous network calls (fine inside a background Worker),
 * capped attempts.
 */
public class SendRetryWorker extends Worker {

    private static final String TAG = "SendRetryWorker";
    private static final int MAX_ATTEMPTS = 10;

    public SendRetryWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        if (!NetworkUtils.isNetworkAvailable(context)) {
            // Constraint should already prevent this, but double-check.
            return Result.retry();
        }

        OutboxDao outboxDao = new OutboxDao(context);
        List<OutboxDao.OutboxItem> pending = outboxDao.getPending();
        if (pending.isEmpty()) {
            return Result.success();
        }

        String serverUrl;
        try {
            serverUrl = ConfigLoader.getServerUrl(context);
        } catch (Exception e) {
            Log.w(TAG, "Impossible de lire l'URL serveur, nouvelle tentative plus tard.", e);
            return Result.retry();
        }

        ApiService apiService = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .client(NetworkClientProvider.get(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);

        int sentCount = 0;
        for (OutboxDao.OutboxItem item : pending) {
            try {
                ApiService.DirectMessageRequest request =
                        new ApiService.DirectMessageRequest(item.phoneNumber, item.content);
                Response<ResponseBody> response = apiService.sendEncryptedMessage(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    NetworkUtils.readJsonOrThrow(response.body()); // throws if not genuine JSON
                    outboxDao.markSent(item.id);
                    sentCount++;
                } else {
                    outboxDao.markRetryFailed(item.id, item.attempts, MAX_ATTEMPTS);
                }
            } catch (Exception e) {
                Log.w(TAG, "Echec de la relance pour l'élément outbox #" + item.id, e);
                outboxDao.markRetryFailed(item.id, item.attempts, MAX_ATTEMPTS);
            }
        }

        Log.i(TAG, "Relance terminée: " + sentCount + "/" + pending.size() + " message(s) envoyé(s).");
        return Result.success();
    }
}
