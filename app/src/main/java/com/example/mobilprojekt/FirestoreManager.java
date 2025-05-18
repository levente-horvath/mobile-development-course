package com.example.mobilprojekt;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

public class FirestoreManager {
    private static final String TAG = "FirestoreManager";
    private static final String COLLECTION_READINGS = "readings";
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public FirestoreManager() {
        try {
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firestore or Auth: " + e.getMessage());
        }
    }

    // Get the current user ID
    private String getCurrentUserId() {
        try {
            if (auth == null) {
                Log.e(TAG, "FirebaseAuth is null");
                return null;
            }
            
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                return user.getUid();
            } else {
                Log.e(TAG, "Current user is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting current user: " + e.getMessage());
        }
        return null;
    }

    // Create a new reading
    public Task<DocumentReference> createReading(MeterReading reading) {
        try {
            if (db == null) {
                Log.e(TAG, "FirebaseFirestore is null");
                return null;
            }
            
            String userId = getCurrentUserId();
            if (userId != null && reading != null) {
                reading.setUserId(userId);
                return db.collection(COLLECTION_READINGS).add(reading);
            } else {
                Log.e(TAG, "User ID or reading is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating reading: " + e.getMessage());
        }
        return null;
    }

    // Read a specific reading
    public Task<QuerySnapshot> getReading(String readingId) {
        try {
            if (db == null) {
                Log.e(TAG, "FirebaseFirestore is null");
                return null;
            }
            
            if (readingId == null || readingId.isEmpty()) {
                Log.e(TAG, "Reading ID is null or empty");
                return null;
            }
            
            String userId = getCurrentUserId();
            if (userId != null) {
                return db.collection(COLLECTION_READINGS)
                        .whereEqualTo("id", readingId)
                        .whereEqualTo("userId", userId)
                        .get();
            } else {
                Log.e(TAG, "User ID is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting reading: " + e.getMessage());
        }
        return null;
    }

    // Update a reading
    public Task<Void> updateReading(String readingId, MeterReading reading) {
        try {
            if (db == null) {
                Log.e(TAG, "FirebaseFirestore is null");
                return null;
            }
            
            if (readingId == null || readingId.isEmpty()) {
                Log.e(TAG, "Reading ID is null or empty");
                return null;
            }
            
            if (reading == null) {
                Log.e(TAG, "Reading is null");
                return null;
            }
            
            String userId = getCurrentUserId();
            if (userId != null && userId.equals(reading.getUserId())) {
                return db.collection(COLLECTION_READINGS).document(readingId).set(reading);
            } else {
                Log.e(TAG, "User ID is null or doesn't match reading user ID");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating reading: " + e.getMessage());
        }
        return null;
    }

    // Delete a reading
    public Task<Void> deleteReading(String readingId) {
        try {
            if (db == null) {
                Log.e(TAG, "FirebaseFirestore is null");
                return null;
            }
            
            if (readingId == null || readingId.isEmpty()) {
                Log.e(TAG, "Reading ID is null or empty");
                return null;
            }
            
            return db.collection(COLLECTION_READINGS).document(readingId).delete();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting reading: " + e.getMessage());
        }
        return null;
    }

    // Get all readings for current user
    public Task<QuerySnapshot> getAllReadings() {
        try {
            if (db == null) {
                Log.e(TAG, "FirebaseFirestore is null");
                return null;
            }
            
            String userId = getCurrentUserId();
            if (userId != null) {
                return db.collection(COLLECTION_READINGS)
                        .whereEqualTo("userId", userId)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get();
            } else {
                Log.e(TAG, "User ID is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all readings: " + e.getMessage());
        }
        return null;
    }

    // Complex Query 1: Get readings from a specific date range
    public Task<QuerySnapshot> getReadingsInDateRange(Date startDate, Date endDate) {
        try {
            if (db == null) {
                Log.e(TAG, "FirebaseFirestore is null");
                return null;
            }
            
            if (startDate == null || endDate == null) {
                Log.e(TAG, "Start date or end date is null");
                return null;
            }
            
            String userId = getCurrentUserId();
            if (userId != null) {
                return db.collection(COLLECTION_READINGS)
                        .whereEqualTo("userId", userId)
                        .whereGreaterThanOrEqualTo("timestamp", startDate)
                        .whereLessThanOrEqualTo("timestamp", endDate)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get();
            } else {
                Log.e(TAG, "User ID is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting readings in date range: " + e.getMessage());
        }
        return null;
    }

    // Complex Query 2: Get readings above a certain value and within a date range
    public Task<QuerySnapshot> getHighReadings(String minReading, Date startDate) {
        try {
            if (db == null) {
                Log.e(TAG, "FirebaseFirestore is null");
                return null;
            }
            
            if (minReading == null || minReading.isEmpty() || startDate == null) {
                Log.e(TAG, "Min reading or start date is null");
                return null;
            }
            
            String userId = getCurrentUserId();
            if (userId != null) {
                return db.collection(COLLECTION_READINGS)
                        .whereEqualTo("userId", userId)
                        .whereGreaterThanOrEqualTo("reading", minReading)
                        .whereGreaterThanOrEqualTo("timestamp", startDate)
                        .orderBy("reading", Query.Direction.DESCENDING)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(10)
                        .get();
            } else {
                Log.e(TAG, "User ID is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting high readings: " + e.getMessage());
        }
        return null;
    }

    // Complex Query 3: Get unsubmitted readings
    public Task<QuerySnapshot> getUnsubmittedReadings() {
        try {
            if (db == null) {
                Log.e(TAG, "FirebaseFirestore is null");
                return null;
            }
            
            String userId = getCurrentUserId();
            if (userId != null) {
                return db.collection(COLLECTION_READINGS)
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("isSubmitted", false)
                        .orderBy("timestamp", Query.Direction.ASCENDING)
                        .get();
            } else {
                Log.e(TAG, "User ID is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting unsubmitted readings: " + e.getMessage());
        }
        return null;
    }

    // Get collection reference
    public CollectionReference getReadingsCollection() {
        try {
            if (db == null) {
                Log.e(TAG, "FirebaseFirestore is null");
                return null;
            }
            
            return db.collection(COLLECTION_READINGS);
        } catch (Exception e) {
            Log.e(TAG, "Error getting collection reference: " + e.getMessage());
            return null;
        }
    }
} 