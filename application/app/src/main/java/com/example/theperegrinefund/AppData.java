package com.example.theperegrinefund;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.theperegrinefund.dao.MessageDao;

/**
 * Holds the id of the currently logged-in mobile app user (userapp.iduserapp).
 *
 * The id is kept both in memory (fast access while the process is alive) and
 * in SharedPreferences (so a new login can detect that a *different* user is
 * now signing in on this device, even after the app process was restarted).
 *
 * Privacy: several rangers can share the same physical phone. If a new user
 * logs in, any locally cached message rows tagged with a different
 * iduserapp are purged so one agent can never browse another agent's
 * history/stats on shared hardware.
 */
public class AppData {

    private static final String TAG = "AppData";
    private static final String PREFS_NAME = "app_data_prefs";
    private static final String KEY_CURRENT_USER_ID = "current_user_id";

    private static int currentUserId = -1;

    private AppData() {
    }

    /**
     * @deprecated kept for backward compatibility with call sites that do not
     * yet have a Context; prefer {@link #getCurrentUserId(Context)} which also
     * survives process restarts.
     */
    @Deprecated
    public static int getCurrentUserId() {
        return currentUserId;
    }

    /**
     * @deprecated kept for backward compatibility; prefer
     * {@link #setCurrentUserId(Context, int)} which persists the id and purges
     * stale data belonging to a previous user.
     */
    @Deprecated
    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    /** Returns the current user id, restoring it from SharedPreferences if needed. */
    public static synchronized int getCurrentUserId(Context context) {
        if (currentUserId > 0) {
            return currentUserId;
        }
        if (context == null) {
            return currentUserId;
        }
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_CURRENT_USER_ID, -1);
        return currentUserId;
    }

    /**
     * Records the newly logged-in user id. If a different user was previously
     * logged in on this device, purges any locally cached message rows that
     * belong to that previous user before storing the new id.
     */
    public static synchronized void setCurrentUserId(Context context, int userId) {
        if (context == null) {
            currentUserId = userId;
            return;
        }
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int previousUserId = prefs.getInt(KEY_CURRENT_USER_ID, -1);

        if (previousUserId > 0 && userId > 0 && previousUserId != userId) {
            purgeOtherUsersData(context, userId);
        }

        currentUserId = userId;
        prefs.edit().putInt(KEY_CURRENT_USER_ID, userId).apply();
    }

    /** Deletes locally cached messages that do not belong to {@code keepUserId}. */
    private static void purgeOtherUsersData(Context context, int keepUserId) {
        try {
            SQLiteDatabase db = new MyDatabaseHelper(context).getWritableDatabase();
            int deleted = db.delete(
                    MyDatabaseHelper.TABLE_MESSAGE,
                    MyDatabaseHelper.COLUMN_USER_FK + " != ?",
                    new String[]{String.valueOf(keepUserId)}
            );
            Log.i(TAG, "Nouvel utilisateur détecté sur cet appareil: " + deleted
                    + " message(s) d'un autre utilisateur purgé(s) localement.");
            db.close();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la purge des données locales d'un autre utilisateur", e);
        }
    }
}
