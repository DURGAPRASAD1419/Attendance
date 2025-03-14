package com.example.attendanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });

        checkUserAuthentication();
    }

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
