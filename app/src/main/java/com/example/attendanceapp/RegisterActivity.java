package com.example.attendanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextName, editTextPhone;
    private Button buttonSendOtp;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // UI Elements
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonSendOtp = findViewById(R.id.buttonSendOtp);
        TextView textViewLogin = findViewById(R.id.textViewLogin);

        // Handle "Already have an account?" click
        textViewLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        buttonSendOtp.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(RegisterActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                return;
            }

            checkUserExists(name, phone);
        });
    }

    private void checkUserExists(String name, String phone) {
        databaseReference.orderByChild("phone").equalTo(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // If user already exists, show a message
                            Toast.makeText(RegisterActivity.this, "User already registered!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Save user and proceed to OTP verification
                            saveUserAndProceed(name, phone);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RegisterActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserAndProceed(String name, String phone) {
        // Save user details before OTP verification
        String userId = databaseReference.push().getKey();
        if (userId != null) {
            databaseReference.child(userId).child("name").setValue(name);
            databaseReference.child(userId).child("phone").setValue(phone);
        }

        // Proceed to OTP verification
        Intent intent = new Intent(RegisterActivity.this, VerifyOtpActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("phone", phone);
        startActivity(intent);
    }
}
