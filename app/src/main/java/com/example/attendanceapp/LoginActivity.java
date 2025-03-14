package com.example.attendanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText usernameEditText = findViewById(R.id.usernameEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        Button proceedButton = findViewById(R.id.proceedButton);

        proceedButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Close LoginActivity
        });
    }
}