package com.example.mobilprojekt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "HistoryActivity";
    private RecyclerView recyclerViewHistory;
    private Button buttonBack;
    private Button buttonShowAll;
    private Button buttonFilterHigh;
    private Button buttonFilterLast3Months;
    private Button buttonFilterWithPhoto;
    private ReadingHistoryAdapter adapter;
    private List<MeterReading> readings;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize FirebaseFirestore and userId
        try {
            db = FirebaseFirestore.getInstance();
            userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : null;

            if (userId == null) {
                Toast.makeText(this, "Nincs bejelentkezett felhasználó!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
        } catch (Exception e) {
            Log.e("HistoryActivity", "Hiba FirebaseFirestore inicializálásakor: " + e.getMessage());
            Toast.makeText(this, "Hiba az adatbázis elérése során!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        buttonBack = findViewById(R.id.buttonBack);
        buttonShowAll = findViewById(R.id.buttonShowAll);
        buttonFilterHigh = findViewById(R.id.buttonFilterHigh);
        buttonFilterLast3Months = findViewById(R.id.buttonFilterLast3Months);
        buttonFilterWithPhoto = findViewById(R.id.buttonFilterWithPhoto);

        readings = new ArrayList<>();
        adapter = new ReadingHistoryAdapter(readings);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(adapter);

        // Set up filter buttons
        buttonShowAll.setOnClickListener(view -> loadAllReadings());
        buttonFilterHigh.setOnClickListener(view -> filterHighReadings());
        buttonFilterLast3Months.setOnClickListener(view -> filterLast3Months());
        buttonFilterWithPhoto.setOnClickListener(view -> filterWithPhotos());

        buttonBack.setOnClickListener(view -> finish());

        // Load readings initially
        loadAllReadings();
    }

    private void loadAllReadings() {
        updateButtonColors(buttonShowAll);
        readings.clear();
        
        try {
            db.collection("readings")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                MeterReading reading = document.toObject(MeterReading.class);
                                if (reading != null) {
                                    readings.add(reading);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HistoryActivity", "Hiba az olvasások lekérdezése során: " + e.getMessage());
                        Toast.makeText(HistoryActivity.this, "Nem sikerült letölteni az előzményeket!", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e("HistoryActivity", "Hiba a Firestore adatok lekérdezésekor: " + e.getMessage());
            Toast.makeText(this, "Hiba az adatok lekérdezésekor!", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterHighReadings() {
        updateButtonColors(buttonFilterHigh);
        readings.clear();
        
        try {
            db.collection("readings")
                    .whereEqualTo("userId", userId)
                    .orderBy("value", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                MeterReading reading = document.toObject(MeterReading.class);
                                if (reading != null) {
                                    readings.add(reading);
                                }
                            }
                            adapter.notifyDataSetChanged();
                            Toast.makeText(HistoryActivity.this, "A 10 legmagasabb érték", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HistoryActivity", "Hiba a magas olvasások lekérdezése során: " + e.getMessage());
                        Toast.makeText(HistoryActivity.this, "Nem sikerült letölteni a magas értékeket!", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e("HistoryActivity", "Hiba a Firestore adatok lekérdezésekor: " + e.getMessage());
            Toast.makeText(this, "Hiba az adatok lekérdezésekor!", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterLast3Months() {
        updateButtonColors(buttonFilterLast3Months);
        readings.clear();
        
        try {
            // Calculate date 3 months ago
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -3);
            Date threeMonthsAgo = calendar.getTime();
            
            db.collection("readings")
                    .whereEqualTo("userId", userId)
                    .whereGreaterThan("timestamp", threeMonthsAgo.getTime())
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                MeterReading reading = document.toObject(MeterReading.class);
                                if (reading != null) {
                                    readings.add(reading);
                                }
                            }
                            adapter.notifyDataSetChanged();
                            Toast.makeText(HistoryActivity.this, "Az elmúlt 3 hónap olvasásai", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(HistoryActivity.this, "Nincsenek olvasások az elmúlt 3 hónapban", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HistoryActivity", "Hiba az olvasások lekérdezése során: " + e.getMessage());
                        Toast.makeText(HistoryActivity.this, "Nem sikerült letölteni az előzményeket!", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e("HistoryActivity", "Hiba a Firestore adatok lekérdezésekor: " + e.getMessage());
            Toast.makeText(this, "Hiba az adatok lekérdezésekor!", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterWithPhotos() {
        updateButtonColors(buttonFilterWithPhoto);
        readings.clear();
        
        try {
            db.collection("readings")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                MeterReading reading = document.toObject(MeterReading.class);
                                if (reading != null && reading.getPhotoUrl() != null && !reading.getPhotoUrl().isEmpty()) {
                                    readings.add(reading);
                                }
                            }
                            
                            if (readings.isEmpty()) {
                                Toast.makeText(HistoryActivity.this, "Nincsenek olvasások fotóval", Toast.LENGTH_SHORT).show();
                            } else {
                                adapter.notifyDataSetChanged();
                                Toast.makeText(HistoryActivity.this, "Fotóval rendelkező olvasások", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HistoryActivity", "Hiba az olvasások lekérdezése során: " + e.getMessage());
                        Toast.makeText(HistoryActivity.this, "Nem sikerült letölteni az előzményeket!", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e("HistoryActivity", "Hiba a Firestore adatok lekérdezésekor: " + e.getMessage());
            Toast.makeText(this, "Hiba az adatok lekérdezésekor!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateButtonColors(Button selectedButton) {
        // Reset all buttons to secondary color
        buttonShowAll.setBackgroundColor(getResources().getColor(R.color.secondary));
        buttonFilterHigh.setBackgroundColor(getResources().getColor(R.color.secondary));
        buttonFilterLast3Months.setBackgroundColor(getResources().getColor(R.color.secondary));
        buttonFilterWithPhoto.setBackgroundColor(getResources().getColor(R.color.secondary));
        
        // Set selected button to primary color
        selectedButton.setBackgroundColor(getResources().getColor(R.color.primary));
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
} 