package com.example.attendanceapp;

import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Executor;

public class FingerprintAuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private ProgressBar progressBar;
    private TextView instructionText;
    private ImageView fingerprintAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print_auth);

        // Firebase Authentication & Database
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // UI Elements
        fingerprintAnimation = findViewById(R.id.fingerprintAnimation);
        progressBar = findViewById(R.id.progressBar);
        instructionText = findViewById(R.id.instructionText);
        Button cancelButton = findViewById(R.id.cancelButton);

        // Load Fingerprint Animation
        Animation bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.fingerprint_bounce);
        fingerprintAnimation.startAnimation(bounceAnimation);

        // Start authentication on click
        fingerprintAnimation.setOnClickListener(v -> startFingerprintAuth());

        // Cancel Button closes activity
        cancelButton.setOnClickListener(v -> finish());
    }

    private void startFingerprintAuth() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                authenticateUser();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "No fingerprint sensor found.", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Fingerprint sensor is not available.", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "No fingerprint enrolled. Please set up fingerprints.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void authenticateUser() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                instructionText.setText("Authentication successful!");
                saveAuthStatusToFirebase();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(FingerprintAuthActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setSubtitle("Use fingerprint to authenticate")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void saveAuthStatusToFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String phoneNumber = user.getPhoneNumber();
            String userId = user.getUid();
            long timestamp = System.currentTimeMillis();

            String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date(timestamp));

            DatabaseReference userRef = databaseReference.child(userId);

            // Fetch user name and update Firebase
            userRef.child("name").get().addOnSuccessListener(dataSnapshot -> {
                String userName = dataSnapshot.exists() ? dataSnapshot.getValue(String.class) : "Unknown User";

                // Store authentication details in Firebase
                HashMap<String, Object> updateData = new HashMap<>();
                updateData.put("phoneNumber", phoneNumber);
                updateData.put("name", userName);
                updateData.put("authStatus", "Authenticated");
                updateData.put("lastAuthenticated", formattedTime);

                userRef.updateChildren(updateData)
                        .addOnSuccessListener(aVoid -> Log.d("FingerprintAuth", "User authentication saved: " + userName))
                        .addOnFailureListener(e -> Log.e("FingerprintAuth", "Failed to save authentication status", e));

                sendSuccessSMS(phoneNumber);
            }).addOnFailureListener(e -> Log.e("FingerprintAuth", "Failed to fetch user name", e));
        }
    }

    private void sendSuccessSMS(String phoneNumber) {
        // 🔹 Implement actual SMS API (Twilio, Nexmo, Firebase Functions, etc.)
        Log.d("FingerprintAuth", "SMS Sent to " + phoneNumber);
    }
}
