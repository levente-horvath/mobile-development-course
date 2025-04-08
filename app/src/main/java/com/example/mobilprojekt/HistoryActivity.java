package com.example.mobilprojekt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;

    private Button buttonBack;

    private ReadingHistoryAdapter adapter;

    private List<MeterReading> readingsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        buttonBack = findViewById(R.id.buttonBack);

        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        
        readingsList = getSampleReadingData();
        
        adapter = new ReadingHistoryAdapter(readingsList);
        recyclerViewHistory.setAdapter(adapter);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
    
    // Teszteléshez
    private List<MeterReading> getSampleReadingData() {
        List<MeterReading> readings = new ArrayList<>();
        
        readings.add(new MeterReading("12345", "2025.01.01"));
        readings.add(new MeterReading("6972420", "2025.02.01"));
        
        return readings;
    }
} 