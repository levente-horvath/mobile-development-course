package com.example.mobilprojekt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GasMeterActivity extends AppCompatActivity {
    
    private static final String TAG = "GasMeterActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_CAMERA_ACTIVITY = 102;
    
    private EditText editTextMeterReading;
    private EditText editTextNotes;
    private DatePicker datePicker;
    private Button buttonSubmitReading;
    private Button buttonViewHistory;
    private Button buttonLogout;
    private ImageButton buttonTakePhoto;
    private ImageView imageViewMeter;
    private TextView textViewLocation;
    
    private FirebaseAuth mAuth;
    private FirestoreManager firestoreManager;
    private FusedLocationProviderClient fusedLocationClient;
    
    private Double latitude;
    private Double longitude;
    private String photoPath;
    private String address;
    
    private FirebaseStorage storage;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gas_meter);
        
        try {
            initializeComponents();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing components: " + e.getMessage());
            Toast.makeText(this, "Hiba az alkalmazás indításakor", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void initializeComponents() {
        mAuth = FirebaseAuth.getInstance();
        firestoreManager = new FirestoreManager();
        storage = FirebaseStorage.getInstance();
        
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        } catch (Exception e) {
            Log.e(TAG, "Error getting location services: " + e.getMessage());
        }
        
        editTextMeterReading = findViewById(R.id.editTextMeterReading);
        datePicker = findViewById(R.id.datePicker);
        buttonSubmitReading = findViewById(R.id.buttonSubmitReading);
        buttonViewHistory = findViewById(R.id.buttonViewHistory);
        buttonLogout = findViewById(R.id.buttonLogout);
        editTextNotes = findViewById(R.id.editTextNotes);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
        imageViewMeter = findViewById(R.id.imageViewMeter);
        textViewLocation = findViewById(R.id.textViewLocation);
        
        try {
            ReminderScheduler.scheduleMonthlyReminder(this, 20);
            WorkManagerHelper.scheduleMeterReadingSync(this);
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling tasks: " + e.getMessage());
        }
        
        if (checkLocationPermission()) {
            try {
                getLastLocation();
            } catch (Exception e) {
                Log.e(TAG, "Error getting location: " + e.getMessage());
            }
        }
        
        if (buttonSubmitReading != null) {
            buttonSubmitReading.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitReading();
                    applyFadeAnimation(buttonSubmitReading);
                    
                    if (imageViewMeter != null && imageViewMeter.getVisibility() == View.VISIBLE) {
                        try {
                            Animation slideOut = AnimationUtils.loadAnimation(GasMeterActivity.this, R.anim.slide_out_right);
                            imageViewMeter.startAnimation(slideOut);
                            slideOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {}
                                
                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    imageViewMeter.setVisibility(View.GONE);
                                }
                                
                                @Override
                                public void onAnimationRepeat(Animation animation) {}
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error animating: " + e.getMessage());
                            if (imageViewMeter != null) {
                                imageViewMeter.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });
        }
        
        if (buttonViewHistory != null) {
            buttonViewHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(GasMeterActivity.this, HistoryActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to history: " + e.getMessage());
                        Toast.makeText(GasMeterActivity.this, "Nem sikerült megnyitni az előzményeket", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        
        if (buttonLogout != null) {
            buttonLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Log.d(TAG, "Logout button clicked, attempting to sign out");
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            Log.d(TAG, "User is logged in: " + currentUser.getUid());
                        } else {
                            Log.d(TAG, "No user is logged in");
                        }
                        
                        WorkManagerHelper.cancelMeterReadingSync(GasMeterActivity.this);
                        
                        mAuth = FirebaseAuth.getInstance();
                        
                        mAuth.signOut();
                        Log.d(TAG, "signOut completed");
                        
                        FirebaseUser postLogoutUser = mAuth.getCurrentUser();
                        if (postLogoutUser == null) {
                            Log.d(TAG, "Logout successful, user is null");
                        } else {
                            Log.e(TAG, "Warning: User still logged in after signOut call: " + postLogoutUser.getUid());
                        }
                        
                        Intent intent = new Intent(GasMeterActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Log.d(TAG, "Starting LoginActivity");
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error during logout: " + e.getMessage());
                        Toast.makeText(GasMeterActivity.this, "Kijelentkezési hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        
        if (buttonTakePhoto != null) {
            buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkCameraPermission()) {
                        try {
                            openCamera();
                        } catch (Exception e) {
                            Log.e(TAG, "Error opening camera: " + e.getMessage());
                            Toast.makeText(GasMeterActivity.this, "Nem sikerült megnyitni a kamerát", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Intent intent = new Intent(GasMeterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking user login state: " + e.getMessage());
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        try {
            getLastLocation();
        } catch (Exception e) {
            Log.e(TAG, "Error getting location on resume: " + e.getMessage());
        }
    }
    
    private boolean checkLocationPermission() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error checking location permission: " + e.getMessage());
            return false;
        }
    }
    
    private boolean checkCameraPermission() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error checking camera permission: " + e.getMessage());
            return false;
        }
    }
    
    private void getLastLocation() {
        if (fusedLocationClient == null) {
            Log.e(TAG, "FusedLocationProviderClient not initialized");
            if (textViewLocation != null) {
                textViewLocation.setText("Helymeghatározás nem elérhető");
            }
            return;
        }
        
        if (checkLocationPermission()) {
            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    
                                    // Get address from location
                                    getAddressFromLocation(location);
                                    
                                    if (textViewLocation != null) {
                                        textViewLocation.setText("Helyzet: " + 
                                                String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude));
                                    }
                                } else {
                                    Log.d(TAG, "Location is null");
                                    if (textViewLocation != null) {
                                        textViewLocation.setText("Helymeghatározás sikertelen, próbálja újra");
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Error getting location: " + e.getMessage());
                                if (textViewLocation != null) {
                                    textViewLocation.setText("Helymeghatározási hiba");
                                }
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Error requesting location: " + e.getMessage());
                if (textViewLocation != null) {
                    textViewLocation.setText("Helymeghatározási hiba");
                }
            }
        }
    }
    
    private void getAddressFromLocation(Location location) {
        if (location == null) return;
        
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && addresses.size() > 0) {
                    Address addr = addresses.get(0);
                    address = addr.getAddressLine(0);
                    
                    if (address != null && textViewLocation != null) {
                        textViewLocation.setText("Helyzet: " + address);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error getting address: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing geocoder: " + e.getMessage());
        }
    }
    
    private void openCamera() {
        try {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivityForResult(intent, REQUEST_CAMERA_ACTIVITY);
        } catch (Exception e) {
            Log.e(TAG, "Error launching camera: " + e.getMessage());
            Toast.makeText(this, "Nem sikerült elindítani a kamerát", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            
            if (requestCode == REQUEST_CAMERA_ACTIVITY && resultCode == RESULT_OK) {
                if (data != null) {
                    photoPath = data.getStringExtra("photo_path");
                    if (photoPath != null && imageViewMeter != null) {
                        try {
                            // Show the image in the ImageView
                            imageViewMeter.setImageURI(android.net.Uri.parse(photoPath));
                            imageViewMeter.setVisibility(View.VISIBLE);
                            
                            // Apply animation to the image
                            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
                            imageViewMeter.startAnimation(slideIn);
                            
                            Toast.makeText(this, "Fotó elkészült", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e(TAG, "Error showing photo: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in activity result: " + e.getMessage());
        }
    }
    
    private void submitReading() {
        try {
            if (editTextMeterReading == null) {
                Toast.makeText(this, "Adatbeviteli hiba", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String reading = editTextMeterReading.getText().toString().trim();
            if (reading.isEmpty()) {
                editTextMeterReading.setError("Óraállás megadása kötelező");
                return;
            }
            
            if (datePicker == null) {
                Toast.makeText(this, "Dátumválasztó nem elérhető", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth();
            int year = datePicker.getYear();
            
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateString = sdf.format(calendar.getTime());
            
            String notes = "";
            if (editTextNotes != null) {
                notes = editTextNotes.getText().toString().trim();
            }
            
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // Create new reading object
                MeterReading meterReading = new MeterReading(reading, dateString, currentUser.getUid(),
                        latitude, longitude, photoPath);
                meterReading.setAddress(address);
                meterReading.setNotes(notes);
                
                // Save to Firestore
                saveReadingToFirestore(meterReading);
            } else {
                Toast.makeText(this, "Nincs bejelentkezett felhasználó", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "No signed in user");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error submitting reading: " + e.getMessage());
            Toast.makeText(this, "Hiba az adatok mentése közben", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveReadingToFirestore(MeterReading reading) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Nincs bejelentkezett felhasználó", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simply pass the local file path to saveReadingWithPhotoUrl instead of uploading to Firebase Storage
        String localPhotoPath = reading.getPhotoUrl();
        saveReadingWithPhotoUrl(reading, user, localPhotoPath);
    }

    private void saveReadingWithPhotoUrl(MeterReading reading, FirebaseUser user, String localPhotoPath) {
        Map<String, Object> readingData = new HashMap<>();
        readingData.put("value", reading.getReading());
        readingData.put("timestamp", reading.getTimestamp());
        readingData.put("latitude", reading.getLatitude());
        readingData.put("longitude", reading.getLongitude());
        readingData.put("notes", reading.getNotes());
        readingData.put("photoUrl", localPhotoPath); // Store the local file path directly
        readingData.put("submitted", reading.isSubmitted());
        readingData.put("userId", user.getUid());

        firestoreManager.createReading(reading)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Reading saved with ID: " + documentReference.getId());
                Toast.makeText(GasMeterActivity.this, "Mérés sikeresen mentve", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving reading", e);
                Toast.makeText(GasMeterActivity.this, "Hiba a mérés mentésekor", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void clearForm() {
        try {
            if (editTextMeterReading != null) {
                editTextMeterReading.setText("");
            }
            
            if (editTextNotes != null) {
                editTextNotes.setText("");
            }
            
            if (imageViewMeter != null) {
                imageViewMeter.setVisibility(View.GONE);
            }
            
            photoPath = null;
        } catch (Exception e) {
            Log.e(TAG, "Error clearing form: " + e.getMessage());
        }
    }
    
    private void applyFadeAnimation(View view) {
        if (view == null) return;
        
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Error applying animation: " + e.getMessage());
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            
            if (requestCode == REQUEST_LOCATION_PERMISSION) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                } else {
                    Toast.makeText(this, "Helymeghatározás engedély elutasítva", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Kamera engedély elutasítva", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling permission result: " + e.getMessage());
        }
    }
} 