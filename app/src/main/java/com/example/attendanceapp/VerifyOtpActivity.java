package com.example.attendanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class VerifyOtpActivity extends AppCompatActivity {
    private EditText editTextOtp;
    private Button buttonVerify;
    private FirebaseAuth mAuth;
    private String verificationId;
    private String name, phone;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        editTextOtp = findViewById(R.id.otpEditText);
        buttonVerify = findViewById(R.id.verifyOtpButton);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        verificationId = getIntent().getStringExtra("verificationId");

        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = editTextOtp.getText().toString().trim();

                if (TextUtils.isEmpty(otp)) {
                    Toast.makeText(VerifyOtpActivity.this, "Enter OTP", Toast.LENGTH_SHORT).show();
                    return;
                }

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            databaseReference.child(user.getUid()).child("name").setValue(name);
                            databaseReference.child(user.getUid()).child("phone").setValue(phone);
                            Toast.makeText(VerifyOtpActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(VerifyOtpActivity.this, HomeActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(VerifyOtpActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
