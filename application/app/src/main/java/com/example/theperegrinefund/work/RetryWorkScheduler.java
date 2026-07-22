package com.example.theperegrinefund.work;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Schedules the periodic outbox retry (SendRetryWorker). Safe to call from
 * every activity's onCreate: WorkManager de-dupes via KEEP so calling this
 * repeatedly does not create duplicate periodic jobs, and the NetworkType
 * .CONNECTED constraint makes WorkManager itself run the job opportunistically
 * as soon as connectivity is regained, in addition to the periodic interval.
 */
public final class RetryWorkScheduler {

    private static final String UNIQUE_WORK_NAME = "outbox-send-retry";
    // 15 minutes is WorkManager's minimum allowed periodic interval.
    private static final long INTERVAL_MINUTES = 15;

    private RetryWorkScheduler() {
    }

    public static void schedulePeriodic(Context context) {
        if (context == null) {
            return;
        }
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                SendRetryWorker.class, INTERVAL_MINUTES, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }
}
