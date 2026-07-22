package com.example.theperegrinefund.network;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Single shared OkHttpClient used by every HTTP call the app makes
 * (Retrofit-based ServerSender/ApiService AND the plain-OkHttp SyncService).
 *
 * Centralising this avoids the previous bug where SyncService sent the
 * "ngrok-skip-browser-warning" header but ServerSender's Retrofit client did
 * not, causing ngrok's HTML interstitial page (HTTP 200) to be silently
 * treated as a successful send.
 */
public final class NetworkClientProvider {

    private static final long CONNECT_TIMEOUT_SECONDS = 15;
    private static final long READ_TIMEOUT_SECONDS = 15;
    private static final long WRITE_TIMEOUT_SECONDS = 15;

    private static volatile OkHttpClient client;

    private NetworkClientProvider() {
    }

    public static OkHttpClient get() {
        OkHttpClient result = client;
        if (result == null) {
            synchronized (NetworkClientProvider.class) {
                result = client;
                if (result == null) {
                    client = result = new OkHttpClient.Builder()
                            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .addInterceptor(new NgrokWarningSkipInterceptor())
                            .build();
                }
            }
        }
        return result;
    }

    /** Adds the header ngrok needs to skip its browser-warning interstitial page on every request. */
    private static class NgrokWarningSkipInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request patchedRequest = originalRequest.newBuilder()
                    .header("ngrok-skip-browser-warning", "true")
                    .build();
            return chain.proceed(patchedRequest);
        }
    }
}
