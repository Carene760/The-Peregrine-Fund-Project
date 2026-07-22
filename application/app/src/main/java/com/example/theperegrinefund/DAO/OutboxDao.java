package com.example.theperegrinefund.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.theperegrinefund.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic local outbox/queue for alert and status-update messages that could
 * not be delivered (neither via direct HTTP nor via SMS fallback). Used by
 * ServerSender to record outcomes and by SendRetryWorker (WorkManager) to
 * retry PENDING entries once connectivity is available again.
 *
 * This is intentionally simple (first version of a retry queue, not a full
 * sync engine): no conflict resolution, no dedup beyond the row id.
 */
public class OutboxDao {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_FAILED = "FAILED";

    public static final String TYPE_ALERTE = "ALERTE";
    public static final String TYPE_HISTORIQUE = "HISTORIQUE";

    public static class OutboxItem {
        public long id;
        public String type;
        public String content;
        public String phoneNumber;
        public int idUserApp;
        public String status;
        public int attempts;
    }

    private final MyDatabaseHelper dbHelper;

    public OutboxDao(Context context) {
        dbHelper = new MyDatabaseHelper(context);
    }

    /** Records a send outcome. Pass status = SENT for a successful attempt worth logging, PENDING for retry, FAILED for a terminal failure. */
    public long insert(String type, String content, String phoneNumber, int idUserApp, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_OUTBOX_TYPE, type);
        values.put(MyDatabaseHelper.COLUMN_OUTBOX_CONTENT, content);
        values.put(MyDatabaseHelper.COLUMN_OUTBOX_PHONE, phoneNumber);
        values.put(MyDatabaseHelper.COLUMN_OUTBOX_USER_FK, idUserApp);
        values.put(MyDatabaseHelper.COLUMN_OUTBOX_STATUS, status);
        values.put(MyDatabaseHelper.COLUMN_OUTBOX_ATTEMPTS, 0);
        long id = db.insert(MyDatabaseHelper.TABLE_OUTBOX, null, values);
        db.close();
        return id;
    }

    public List<OutboxItem> getPending() {
        List<OutboxItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                MyDatabaseHelper.TABLE_OUTBOX,
                null,
                MyDatabaseHelper.COLUMN_OUTBOX_STATUS + " = ?",
                new String[]{STATUS_PENDING},
                null, null, null
        );
        while (cursor.moveToNext()) {
            OutboxItem item = new OutboxItem();
            item.id = cursor.getLong(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_OUTBOX_ID));
            item.type = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_OUTBOX_TYPE));
            item.content = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_OUTBOX_CONTENT));
            item.phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_OUTBOX_PHONE));
            item.idUserApp = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_OUTBOX_USER_FK));
            item.status = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_OUTBOX_STATUS));
            item.attempts = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_OUTBOX_ATTEMPTS));
            items.add(item);
        }
        cursor.close();
        db.close();
        return items;
    }

    public void markSent(long id) {
        updateStatus(id, STATUS_SENT, null);
    }

    /** Marks a failed retry attempt; caps retries so a permanently-broken row does not retry forever. */
    public void markRetryFailed(long id, int attemptsSoFar, int maxAttempts) {
        String newStatus = (attemptsSoFar + 1) >= maxAttempts ? STATUS_FAILED : STATUS_PENDING;
        updateStatus(id, newStatus, attemptsSoFar + 1);
    }

    private void updateStatus(long id, String status, Integer attempts) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_OUTBOX_STATUS, status);
        values.put(MyDatabaseHelper.COLUMN_OUTBOX_UPDATED_AT, java.time.LocalDateTime.now().toString());
        if (attempts != null) {
            values.put(MyDatabaseHelper.COLUMN_OUTBOX_ATTEMPTS, attempts);
        }
        db.update(
                MyDatabaseHelper.TABLE_OUTBOX,
                values,
                MyDatabaseHelper.COLUMN_OUTBOX_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        db.close();
    }
}
