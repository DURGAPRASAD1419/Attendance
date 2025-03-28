package com.example.attendanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        mAuth = FirebaseAuth.getInstance();
        Button useFingerprint = findViewById(R.id.fingerprintAuthButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        useFingerprint.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, FingerprintAuthActivity.class)));
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });

        checkUserAuthentication();
    }
   // DatabaseReference userRef = databaseRef.child(userId);
    private void checkUserAuthentication() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        } else {
            String phoneNumber = user.getPhoneNumber();
            Toast.makeText(this, "Logged in as: " + phoneNumber, Toast.LENGTH_SHORT).show();
            Log.d("HomeActivity", "User logged in: " + phoneNumber);
        }
    }
}
