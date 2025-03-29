package com.example.attendanceapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.*;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

public class FingerprintAuthActivity extends AppCompatActivity {
    private DatabaseReference databaseRef;
    private Button cancelButton;
    private ImageView authenticateButton;
    private FusedLocationProviderClient fusedLocationClient;

    private static final double COLLEGE_LATITUDE = 16.829890;
    private static final double COLLEGE_LONGITUDE = 82.036664;
    private static final float GEOFENCE_RADIUS = 200; // 200 meters

    private String userFingerprintKey;
    private String userPhoneNumber;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print_auth);

        databaseRef = FirebaseDatabase.getInstance().getReference("Users");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        authenticateButton = findViewById(R.id.fingerprintAnimation);
        cancelButton = findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(view -> {
            startActivity(new Intent(FingerprintAuthActivity.this, UserLoginActivity.class));
            finish();
        });

        // Fetch user details from Firebase
        fetchUserDetails();

        authenticateButton.setOnClickListener(v -> checkLocationAndAuthenticate());
    }

    private void fetchUserDetails() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean userFound = false;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String phoneNumber = userSnapshot.child("phoneNumber").getValue(String.class);
                    Boolean isLoggedIn = userSnapshot.child("isLoggedIn").getValue(Boolean.class);

                    if (isLoggedIn != null && isLoggedIn) {
                        userFound = true;
                        userPhoneNumber = phoneNumber;
                        userKey = userSnapshot.getKey();
                        userFingerprintKey = userSnapshot.child("fingerprintKey").getValue(String.class);

                        Log.d("FingerprintAuth", "User found: " + userPhoneNumber);
                        Log.d("FingerprintAuth", "Fingerprint Key: " + userFingerprintKey);
                        break;
                    }
                }
                if (!userFound) {
                    Toast.makeText(FingerprintAuthActivity.this, "No user logged in. Please log in again.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FingerprintAuth", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void checkLocationAndAuthenticate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                if (isWithinGeofence(location.getLatitude(), location.getLongitude())) {
                    startFingerprintAuth();
                } else {
                    Toast.makeText(FingerprintAuthActivity.this, "You must be within the college premises to authenticate.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(FingerprintAuthActivity.this, "Unable to get location. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isWithinGeofence(double userLat, double userLng) {
        float[] results = new float[1];
        Location.distanceBetween(userLat, userLng, COLLEGE_LATITUDE, COLLEGE_LONGITUDE, results);
        return results[0] <= GEOFENCE_RADIUS;
    }

    private void startFingerprintAuth() {
        if (userFingerprintKey == null || userFingerprintKey.isEmpty()) {
            Toast.makeText(this, "No fingerprint registered for this user!", Toast.LENGTH_SHORT).show();
            return;
        }

        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Log.d("FingerprintAuth", "Biometric Authentication Succeeded!");

                    if (verifyFingerprint(userFingerprintKey)) {
                        Log.d("FingerprintAuth", "Fingerprint Verified Successfully!");
                        Toast.makeText(FingerprintAuthActivity.this, "Authentication Successful!", Toast.LENGTH_SHORT).show();
                        updateAuthStatus();
                    } else {
                        Log.e("FingerprintAuth", "Fingerprint Verification Failed!");
                        Toast.makeText(FingerprintAuthActivity.this, "Fingerprint does not match!", Toast.LENGTH_SHORT).show();
                    }
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
            Toast.makeText(this, "Fingerprint Authentication not available. Please enroll your fingerprint.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean verifyFingerprint(String storedFingerprintKey) {
        try {
            byte[] publicKeyBytes = Base64.decode(storedFingerprintKey, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update("VerifyFingerprint".getBytes(StandardCharsets.UTF_8));

            return signature.verify(publicKeyBytes); // Signature verification improved
        } catch (Exception e) {
            Log.e("FingerprintAuth", "Error verifying fingerprint: " + e.getMessage());
            return false;
        }
    }

    private void updateAuthStatus() {
        if (userKey == null) return;

        @SuppressLint("SimpleDateFormat")
        String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        databaseRef.child(userKey).child("authStatus").setValue("authenticated");
        databaseRef.child(userKey).child("lastAuthenticated").setValue(formattedTime);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationAndAuthenticate();
        } else {
            Toast.makeText(this, "Permission denied. Cannot proceed with authentication.", Toast.LENGTH_SHORT).show();
        }
    }
}
