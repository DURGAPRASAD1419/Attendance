package com.example.attendanceapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

public class UserLoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        emailEditText = findViewById(R.id.userEmail);
        passwordEditText = findViewById(R.id.userPassword);
        loginButton = findViewById(R.id.btnUserLogin);

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("LoginDebug", "Attempting login with email: " + email);

        // Query Firebase for the specific user with the entered email
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d("LoginDebug", "No matching user found for email: " + email);
                    Toast.makeText(UserLoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String storedPassword = userSnapshot.child("password").getValue(String.class);

                    if (storedPassword != null && storedPassword.equals(password)) {
                        Log.d("LoginDebug", "Login successful for email: " + email);

                        // Update login status in Firebase
                        userSnapshot.getRef().child("isLoggedIn").setValue(true);

                        Toast.makeText(UserLoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UserLoginActivity.this, FingerprintAuthActivity.class));
                        finish();
                        return;
                    }
                }

                Log.d("LoginDebug", "Incorrect password for email: " + email);
                Toast.makeText(UserLoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LoginDebug", "Database Error: " + error.getMessage());
                Toast.makeText(UserLoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
