package com.example.attendanceapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.Executor;

public class FingerprintAuthActivity extends Activity {

    private DatabaseReference databaseRef;
    private String userId = "user1"; // Get this dynamically based on the logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print_auth);

        // Initialize Firebase Database Reference
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        authenticateUserWithFingerprint();
    }

    private void authenticateUserWithFingerprint() {
        // Simulating successful fingerprint authentication
        boolean fingerprintSuccess = true; // Assume success for now

        if (fingerprintSuccess) {
            saveAuthenticationStatus();
        } else {
            Toast.makeText(this, "Fingerprint authentication failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAuthenticationStatus() {
        long timestamp = System.currentTimeMillis();

        // Update the user's authentication status
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("authStatus", "Authenticated");
        updates.put("lastAuthenticated", timestamp);

        databaseRef.child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(FingerprintAuthActivity.this, "Authentication saved", Toast.LENGTH_SHORT).show();
                    sendAuthenticationSMS("+1234567890"); // Replace with actual user's phone number
                })
                .addOnFailureListener(e -> Toast.makeText(FingerprintAuthActivity.this, "Failed to save data", Toast.LENGTH_SHORT).show());
    }

    private void sendAuthenticationSMS(String phoneNumber) {
        // Logic to send SMS using Twilio or Firebase Cloud Messaging (FCM)
        Toast.makeText(this, "SMS sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
    }
}
