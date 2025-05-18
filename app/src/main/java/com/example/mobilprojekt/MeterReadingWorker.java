package com.example.mobilprojekt;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MeterReadingWorker extends Worker {
    private static final String TAG = "MeterReadingWorker";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public MeterReadingWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting background sync of unsubmitted readings");

        // Check if user is logged in
        if (auth.getCurrentUser() == null) {
            Log.d(TAG, "User not logged in, cannot sync readings");
            return Result.failure();
        }

        String userId = auth.getCurrentUser().getUid();
        
        try {
            // Get all unsubmitted readings
            List<MeterReading> unsubmittedReadings = new ArrayList<>();
            
            db.collection("readings")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isSubmitted", false)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MeterReading reading = document.toObject(MeterReading.class);
                                
                                // Mark as submitted
                                reading.setSubmitted(true);
                                
                                // Update in Firestore
                                db.collection("readings")
                                        .document(document.getId())
                                        .set(reading)
                                        .addOnSuccessListener(aVoid -> 
                                                Log.d(TAG, "Reading marked as submitted: " + document.getId()))
                                        .addOnFailureListener(e -> 
                                                Log.w(TAG, "Error updating reading", e));
                            }
                        } else {
                            Log.w(TAG, "Error getting unsubmitted readings", task.getException());
                        }
                    });
            
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error in background sync", e);
            return Result.retry();
        }
    }
} 