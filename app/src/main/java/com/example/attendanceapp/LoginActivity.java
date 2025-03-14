package com.example.attendanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private EditText phoneEditText;
    private Button sendOtpButton;
    private FirebaseAuth mAuth;
    private String verificationId;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneEditText = findViewById(R.id.phoneEditText);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        sendOtpButton.setOnClickListener(v -> {
            String phoneNumber = phoneEditText.getText().toString().trim();
            if (!phoneNumber.isEmpty()) {
                sendVerificationCode(phoneNumber);
            } else {
                Toast.makeText(LoginActivity.this, "Enter phone number", Toast.LENGTH_SHORT).show();
            }
        });

        // Get FCM token and save it
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("FCM", "Fetching FCM token failed", task.getException());
                return;
            }
            String token = task.getResult();
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                saveTokenToDatabase(FirebaseAuth.getInstance().getCurrentUser().getUid(), token);
            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(LoginActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                LoginActivity.this.verificationId = verificationId;
                                Intent intent = new Intent(LoginActivity.this, VerifyOtpActivity.class);
                                intent.putExtra("verificationId", verificationId);
                                intent.putExtra("phoneNumber", phoneNumber);
                                startActivity(intent);
                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        saveTokenToDatabase(userId, "");
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveTokenToDatabase(String userId, String token) {
        databaseRef.child(userId).child("fcmToken").setValue(token);
    }
}
