package com.example.mobilprojekt;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextRegisterEmail, editTextRegisterPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLoginLink;
    
    private FirebaseAuth mAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        mAuth = FirebaseAuth.getInstance();
        
        editTextRegisterEmail = findViewById(R.id.editTextRegisterEmail);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLoginLink = findViewById(R.id.textViewLoginLink);
        
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
        
        textViewLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    
    private void registerUser() {
        String email = editTextRegisterEmail.getText().toString().trim();
        String password = editTextRegisterPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        
        if (TextUtils.isEmpty(email)) {
            editTextRegisterEmail.setError("Email cím megadása kötelező");
            editTextRegisterEmail.requestFocus();
            return;
        }
        
        if (!isValidEmail(email)) {
            editTextRegisterEmail.setError("Kérjük, adjon meg egy érvényes email címet (példa: felhasznalo@pelda.com)");
            editTextRegisterEmail.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            editTextRegisterPassword.setError("Jelszó megadása kötelező");
            editTextRegisterPassword.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            editTextRegisterPassword.setError("A jelszónak legalább 6 karakterből kell állnia");
            editTextRegisterPassword.requestFocus();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("A jelszavak nem egyeznek");
            editTextConfirmPassword.requestFocus();
            return;
        }
        
        buttonRegister.setEnabled(false);
        
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        buttonRegister.setEnabled(true);
                        
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(RegisterActivity.this, "Sikeres regisztráció.", 
                                    Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Sikertelen regisztráció: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 