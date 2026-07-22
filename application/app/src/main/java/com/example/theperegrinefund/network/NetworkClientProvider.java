package com.example.theperegrinefund.network;

import android.content.Context;

import com.example.theperegrinefund.security.ConfigLoader;

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
 *
 * It also attaches the shared "X-API-Key" header the server now requires on
 * /api/** and /sync/** (see ApiKeyInterceptor server-side) so every call
 * site is authenticated the same way without having to remember to add it
 * individually.
 */
public final class NetworkClientProvider {

    private static final long CONNECT_TIMEOUT_SECONDS = 15;
    private static final long READ_TIMEOUT_SECONDS = 15;
    private static final long WRITE_TIMEOUT_SECONDS = 15;

    private static volatile OkHttpClient client;

    private NetworkClientProvider() {
    }

    /** @deprecated prefer {@link #get(Context)} so the X-API-Key header is attached. */
    @Deprecated
    public static OkHttpClient get() {
        return get(null);
    }

    public static OkHttpClient get(Context context) {
        OkHttpClient result = client;
        if (result == null) {
            synchronized (NetworkClientProvider.class) {
                result = client;
                if (result == null) {
                    String apiKey = null;
                    if (context != null) {
                        try {
                            apiKey = ConfigLoader.getApiKey(context);
                        } catch (Exception ignored) {
                            // Falls back to no API key header; server will reject with 401
                            // and the caller's existing HTTP-failure/SMS-fallback path handles it.
                        }
                    }
                    client = result = new OkHttpClient.Builder()
                            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                            .addInterceptor(new NgrokWarningSkipInterceptor())
                            .addInterceptor(new ApiKeyInterceptor(apiKey))
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

    /** Adds the shared API key the server now requires on /api/** and /sync/**. */
    private static class ApiKeyInterceptor implements Interceptor {
        private final String apiKey;

        ApiKeyInterceptor(String apiKey) {
            this.apiKey = apiKey;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            if (apiKey == null || apiKey.isEmpty()) {
                return chain.proceed(originalRequest);
            }
            Request patchedRequest = originalRequest.newBuilder()
                    .header("X-API-Key", apiKey)
                    .build();
            return chain.proceed(patchedRequest);
        }
    }
}
