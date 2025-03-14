package com.example.attendanceapp;

import android.annotation.SuppressLint;
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
import java.util.Locale;
import java.util.concurrent.Executor;

public class FingerprintAuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private ProgressBar progressBar;
    private TextView instructionText;
    private ImageView fingerprintAnimation;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print_auth);

        // Firebase Authentication & Database
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

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
                //progressBar.setVisibility(ProgressBar.VISIBLE);
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
            long timestamp = System.currentTimeMillis();

            // ✅ Convert timestamp to readable Date & Time format
            String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date(timestamp));

            if (phoneNumber != null) {
                DatabaseReference userRef = databaseReference.child(phoneNumber);
                userRef.child("authStatus").setValue("Authenticated");
                userRef.child("lastAuthenticated").setValue(formattedTime);

                Log.d("FingerprintAuth", "User authentication saved at: " + formattedTime);
                sendSuccessSMS(phoneNumber);
            }
        }
    }

    private void sendSuccessSMS(String phoneNumber) {
        // ❌ Fast2SMS is not a built-in Firebase feature.
        // ✅ Replace this with actual SMS service integration.
        Log.d("FingerprintAuth", "SMS Sent to " + phoneNumber);
    }
}