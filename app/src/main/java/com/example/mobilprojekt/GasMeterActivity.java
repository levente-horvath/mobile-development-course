package com.example.mobilprojekt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class GasMeterActivity extends AppCompatActivity {
    
    private EditText editTextMeterReading;
    private DatePicker datePicker;
    private Button buttonSubmitReading;
    private Button buttonViewHistory;
    private Button buttonLogout;
    
    private FirebaseAuth mAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gas_meter);
        
        mAuth = FirebaseAuth.getInstance();
        editTextMeterReading = findViewById(R.id.editTextMeterReading);
        datePicker = findViewById(R.id.datePicker);
        buttonSubmitReading = findViewById(R.id.buttonSubmitReading);
        buttonViewHistory = findViewById(R.id.buttonViewHistory);
        buttonLogout = findViewById(R.id.buttonLogout);
        
        buttonSubmitReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReading();
                applyFadeAnimation(buttonSubmitReading);
            }
        });
        
        buttonViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GasMeterActivity.this, HistoryActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(GasMeterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(GasMeterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
    
    private void submitReading() {
        String reading = editTextMeterReading.getText().toString().trim();
        if (reading.isEmpty()) {
            editTextMeterReading.setError("Óraállás megadása kötelező");
            return;
        }
        
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        
        Toast.makeText(this, "Óraállás rögzítve: " + reading + " dátum: " + 
                day + "/" + (month + 1) + "/" + year, Toast.LENGTH_LONG).show();
        
        editTextMeterReading.setText("");
    }
    
    private void applyFadeAnimation(View view) {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        
        view.startAnimation(fadeIn);
        
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {}
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }
} 