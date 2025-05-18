package com.example.mobilprojekt;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class WorkManagerHelper {
    private static final String TAG = "WorkManagerHelper";
    private static final String READING_SYNC_WORK = "reading_sync_work";

    // Schedule a periodic work to sync meter readings
    public static void scheduleMeterReadingSync(Context context) {
        try {
            // Set constraints - only run when network is connected
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Create a periodic work request to run once a day
            PeriodicWorkRequest syncWorkRequest = new PeriodicWorkRequest.Builder(
                    MeterReadingWorker.class,
                    1, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .build();

            // Enqueue the work
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    READING_SYNC_WORK,
                    ExistingPeriodicWorkPolicy.REPLACE, // Replace any existing work
                    syncWorkRequest
            );
            Log.d(TAG, "Meter reading sync work scheduled successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling WorkManager task: " + e.getMessage(), e);
        }
    }

    // Cancel the scheduled work
    public static void cancelMeterReadingSync(Context context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(READING_SYNC_WORK);
            Log.d(TAG, "Meter reading sync work cancelled successfully.");
        } catch (Exception e) {
            // Log the exception, but don't let it crash the app
            // This can happen if WorkManager is not initialized (e.g., in some test environments or due to manifest issues)
            Log.e(TAG, "Error cancelling WorkManager task: " + e.getMessage(), e);
        }
    }
} 