package com.example.attendanceapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

public class FingerprintAuthActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private Button cancelButton;
    private ImageView authenticateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print_auth);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Find buttons
        authenticateButton = findViewById(R.id.fingerprintAnimation);
        cancelButton = findViewById(R.id.cancelButton);

        // Cancel button returns to HomeActivity
        cancelButton.setOnClickListener(view -> {
            startActivity(new Intent(FingerprintAuthActivity.this, HomeActivity.class));
            finish();
        });

        // Start fingerprint authentication only when button is clicked
        authenticateButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                startFingerprintAuth();
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void startFingerprintAuth() {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(FingerprintAuthActivity.this, "Authentication Successful!", Toast.LENGTH_SHORT).show();

                    String userEmail = mAuth.getCurrentUser().getEmail(); // Get logged-in user's email
                    updateAuthStatus(userEmail); // Pass email to find user in database
                }


                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(FingerprintAuthActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Fingerprint Authentication")
                    .setSubtitle("Authenticate using your fingerprint")
                    .setNegativeButtonText("Cancel")
                    .build();

            biometricPrompt.authenticate(promptInfo);
        } else {
            Toast.makeText(this, "Fingerprint Authentication not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAuthStatus(String email) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userKey = userSnapshot.getKey(); // Get the user key (e.g., "user1")

                        // Format timestamp
                        @SuppressLint("SimpleDateFormat")
                        String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                        // Update authStatus and lastAuthenticated
                        usersRef.child(userKey).child("authStatus").setValue("authenticated");
                        usersRef.child(userKey).child("lastAuthenticated").setValue(formattedTime);

                        Toast.makeText(FingerprintAuthActivity.this, "Authentication updated!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FingerprintAuthActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FingerprintAuthActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
