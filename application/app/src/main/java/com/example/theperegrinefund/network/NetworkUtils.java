package com.example.theperegrinefund.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import okhttp3.ResponseBody;

import java.io.IOException;

/**
 * Small helpers shared by ServerSender/SyncService to decide whether direct
 * HTTP to the server should even be attempted, and to make sure an HTTP 200
 * response is really JSON from our server and not an ngrok/proxy HTML page.
 */
public final class NetworkUtils {

    private NetworkUtils() {
    }

    /** True if the device currently reports a usable network connection (wifi or mobile data). */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.net.Network network = cm.getActiveNetwork();
                if (network == null) {
                    return false;
                }
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                return capabilities != null
                        && (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
                        && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            } else {
                @SuppressWarnings("deprecation")
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Reads the response body as text and throws if it does not look like
     * JSON. Protects against ngrok/proxy interstitial HTML pages returned
     * with HTTP 200, which would otherwise be treated as a successful send.
     */
    public static String readJsonOrThrow(ResponseBody body) throws IOException {
        if (body == null) {
            throw new IOException("Réponse serveur vide");
        }
        String raw = body.string();
        String trimmed = raw == null ? "" : raw.trim();
        if (!trimmed.startsWith("[") && !trimmed.startsWith("{")) {
            String preview = trimmed.length() > 160 ? trimmed.substring(0, 160) + "..." : trimmed;
            throw new IOException("Réponse serveur non JSON (probable page ngrok/proxy): " + preview);
        }
        return trimmed;
    }
}
