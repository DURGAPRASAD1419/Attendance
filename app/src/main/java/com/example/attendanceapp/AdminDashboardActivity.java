package com.example.attendanceapp;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class AdminDashboardActivity extends AppCompatActivity {

    private EditText nameInput, phoneInput, emailInput, passwordInput;
    private Button addUserButton, logoutButton;
    private DatabaseReference database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("users");

        nameInput = findViewById(R.id.userName);
        phoneInput = findViewById(R.id.userPhone);
        emailInput = findViewById(R.id.userEmail);
        passwordInput = findViewById(R.id.userPassword);
        addUserButton = findViewById(R.id.btnAddUser);
        logoutButton = findViewById(R.id.btnLogout);

        addUserButton.setOnClickListener(v -> addUser());
        logoutButton.setOnClickListener(v -> logoutAdmin());
    }

    private void addUser() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (!isValidName(name)) {
            nameInput.setError("Enter a valid name (only letters, min 3 chars)");
            return;
        }
        if (!isValidPhone(phone)) {
            phoneInput.setError("Enter a valid phone number in +91XXXXXXXXXX format");
            return;
        }
        if (!isValidEmail(email)) {
            emailInput.setError("Enter a valid email");
            return;
        }
        if (!isValidPassword(password)) {
            passwordInput.setError("Password must be at least 6 characters");
            return;
        }

        // Check if phone number already exists in Firebase
        database.orderByChild("phoneNumber").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    phoneInput.setError("Phone number already registered!");
                    Toast.makeText(AdminDashboardActivity.this, "User with this phone number already exists!", Toast.LENGTH_SHORT).show();
                } else {
                    saveUserToDatabase(name, phone, email, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminDashboardActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToDatabase(String name, String phone, String email, String password) {
        String userId = database.push().getKey();
        String fingerprintKey = generateFingerprintKey();

        // Log to check if fingerprint key is generated
        Log.d("FingerprintKey", "Generated Fingerprint Key: " + fingerprintKey);

        // Create a new user object
        User newUser = new User(phone, name, email, password, fingerprintKey, "Not Authenticated", "N/A");

        // Save user to Firebase
        database.child(userId).setValue(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminDashboardActivity.this, "User Added!", Toast.LENGTH_SHORT).show();
                    clearInputFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminDashboardActivity.this, "Failed to Add User!", Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseError", "Error: " + e.getMessage()); // Log the error
                });
    }


    // Generate Unique Fingerprint Key
    private String generateFingerprintKey() {
        return UUID.randomUUID().toString(); // Generates a unique fingerprint key
    }

    private boolean isValidName(String name) {
        return name.matches("^[a-zA-Z\\s]{3,}$"); // Only letters & spaces, min 3 chars
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^\\+91[0-9]{10}$"); // Starts with +91 and followed by 10 digits
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches(); // Standard email validation
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 6; // Min 6 characters
    }

    private void clearInputFields() {
        nameInput.setText("");
        phoneInput.setText("");
        emailInput.setText("");
        passwordInput.setText("");
    }

    private void logoutAdmin() {
        auth.signOut(); // Sign out from Firebase Authentication
        Toast.makeText(this, "Logged Out!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close current activity
    }
}
