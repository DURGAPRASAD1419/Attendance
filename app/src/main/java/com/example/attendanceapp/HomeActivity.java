package com.example.attendanceapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.concurrent.Executor;

public class HomeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView statusText;
    private Button scanButton;
    private ProgressBar progressBar;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private static final double COLLEGE_LAT = 16.9860; // Replace with your college latitude
    private static final double COLLEGE_LON = 82.2351; // Replace with your college longitude
    private static final float GEOFENCE_RADIUS = 1000; // 200 meters radius

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        statusText = findViewById(R.id.statusText);
        scanButton = findViewById(R.id.scanButton);
        progressBar = findViewById(R.id.progressBar);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permission
        requestLocationPermission();

        // Setup BiometricPrompt
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                statusText.setText("Authentication error: " + errString);
                Toast.makeText(HomeActivity.this, "Error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                statusText.setText("Fingerprint verified. Sending to server...");
                progressBar.setVisibility(ProgressBar.VISIBLE);
                sendAttendanceToServer();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                statusText.setText("Authentication failed");
                Toast.makeText(HomeActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Mark Attendance")
                .setSubtitle("Scan your fingerprint")
                .setNegativeButtonText("Cancel")
                .build();

        scanButton.setOnClickListener(v -> checkLocationAndScan());
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationAndScan();
            } else {
                statusText.setText("Please grant location permission to proceed.");
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkLocationAndScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double currentLat = location.getLatitude();
                            double currentLon = location.getLongitude();
                            float[] distance = new float[1];
                            Location.distanceBetween(currentLat, currentLon, COLLEGE_LAT, COLLEGE_LON, distance);

                            if (distance[0] <= GEOFENCE_RADIUS) {
                                statusText.setText("Within college premises. Scan your fingerprint.");
                                biometricPrompt.authenticate(promptInfo);
                            } else {
                                statusText.setText("You are outside college premises!");
                                Toast.makeText(HomeActivity.this, "Attendance restricted to college premises.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            statusText.setText("Unable to get location");
                            Toast.makeText(HomeActivity.this, "Location unavailable", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendAttendanceToServer() {
        Intent intent = new Intent(HomeActivity.this, StatusActivity.class);
        intent.putExtra("Raju", "Raju123"); // Replace with real faculty ID
        startActivity(intent);
    }
}