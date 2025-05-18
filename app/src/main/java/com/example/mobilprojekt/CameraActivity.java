package com.example.mobilprojekt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 10;
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";

    private PreviewView viewFinder;
    private Button captureButton;
    private Button backButton;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private File outputDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_camera);
            initializeComponents();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing camera activity: " + e.getMessage(), e);
            Toast.makeText(this, "Hiba a kamera inicializálásakor", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }
    
    private void initializeComponents() {
        try {
            // Find views
            viewFinder = findViewById(R.id.viewFinder);
            captureButton = findViewById(R.id.camera_capture_button);
            backButton = findViewById(R.id.camera_back_button);
    
            // Request camera permissions
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CODE_CAMERA_PERMISSION
                );
            }
    
            // Set up the capture button listener
            if (captureButton != null) {
                captureButton.setOnClickListener(v -> {
                    try {
                        takePhoto();
                    } catch (Exception e) {
                        Log.e(TAG, "Error taking photo: " + e.getMessage(), e);
                        Toast.makeText(this, "Hiba a fotó készítésekor", Toast.LENGTH_SHORT).show();
                    }
                });
            }
    
            // Set up the back button listener
            if (backButton != null) {
                backButton.setOnClickListener(v -> {
                    try {
                        setResult(RESULT_CANCELED);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing camera: " + e.getMessage(), e);
                        finish();
                    }
                });
            }
    
            try {
                outputDirectory = getOutputDirectory();
            } catch (Exception e) {
                Log.e(TAG, "Error creating output directory: " + e.getMessage(), e);
                // Use fallback directory
                outputDirectory = getCacheDir();
            }
            
            cameraExecutor = Executors.newSingleThreadExecutor();
        } catch (Exception e) {
            Log.e(TAG, "Error in component initialization: " + e.getMessage(), e);
            Toast.makeText(this, "Nem sikerült inicializálni a kamerát", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Kamera nem elérhető", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create output file
            SimpleDateFormat sdf = new SimpleDateFormat(FILENAME_FORMAT, Locale.US);
            String fileName = "IMG_" + sdf.format(System.currentTimeMillis()) + ".jpg";
            File photoFile = new File(outputDirectory, fileName);
    
            // Create output options object
            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
    
            // Take the picture
            imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            try {
                                String msg = "Fotó sikeresen elmentve: " + photoFile.getAbsolutePath();
                                Log.d(TAG, msg);
                                
                                // Return the file path to the calling activity
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("photo_path", photoFile.getAbsolutePath());
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            } catch (Exception e) {
                                Log.e(TAG, "Error handling saved image: " + e.getMessage(), e);
                                setResult(RESULT_CANCELED);
                                finish();
                            }
                        }
    
                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                            Toast.makeText(CameraActivity.this, "Hiba a fotó mentése közben", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error in photo capture process: " + e.getMessage(), e);
            Toast.makeText(this, "Nem sikerült fotót készíteni", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCamera() {
        try {
            ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
    
            cameraProviderFuture.addListener(() -> {
                try {
                    // Bind the lifecycle of cameras to the lifecycle owner
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
    
                    // Preview
                    Preview preview = new Preview.Builder().build();
                    if (viewFinder != null) {
                        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
                    }
    
                    // Image capture
                    imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build();
    
                    // Select back camera as a default
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    
                    try {
                        // Unbind use cases before rebinding
                        cameraProvider.unbindAll();
        
                        // Bind use cases to camera
                        cameraProvider.bindToLifecycle(
                                this,
                                cameraSelector,
                                preview,
                                imageCapture
                        );
                    } catch (Exception e) {
                        Log.e(TAG, "Use case binding failed: " + e.getMessage(), e);
                        Toast.makeText(this, "Kamera inicializálási hiba", Toast.LENGTH_SHORT).show();
                    }
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Error starting camera: " + e.getMessage(), e);
                    Toast.makeText(this, "Nem sikerült elindítani a kamerát", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error in camera startup: " + e.getMessage(), e);
                    Toast.makeText(this, "Váratlan hiba a kamera indításakor", Toast.LENGTH_SHORT).show();
                }
            }, ContextCompat.getMainExecutor(this));
        } catch (Exception e) {
            Log.e(TAG, "Error getting camera provider: " + e.getMessage(), e);
            Toast.makeText(this, "Kamera szolgáltatás nem elérhető", Toast.LENGTH_SHORT).show();
        }
    }

    private File getOutputDirectory() {
        try {
            File[] mediaDirs = getExternalMediaDirs();
            if (mediaDirs == null || mediaDirs.length == 0) {
                Log.e(TAG, "External media directories not available");
                return getCacheDir();
            }
            
            File mediaDir = mediaDirs[0];
            if (mediaDir == null) {
                Log.e(TAG, "External media directory is null");
                return getCacheDir();
            }
            
            File appDir = new File(mediaDir, getResources().getString(R.string.app_name));
            if (!appDir.exists() && !appDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory");
                return mediaDir;
            }
            return appDir;
        } catch (Exception e) {
            Log.e(TAG, "Error getting output directory: " + e.getMessage(), e);
            return getCacheDir();
        }
    }

    private boolean allPermissionsGranted() {
        try {
            return ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            Log.e(TAG, "Error checking permissions: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
                if (allPermissionsGranted()) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Szükség van a kamera engedélyre", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling permission result: " + e.getMessage(), e);
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            if (cameraExecutor != null) {
                cameraExecutor.shutdown();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error shutting down camera executor: " + e.getMessage(), e);
        }
    }
} 