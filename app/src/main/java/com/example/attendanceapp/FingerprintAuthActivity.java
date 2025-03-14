package com.example.attendanceapp;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FingerprintAuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print_auth);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        authenticateUser();
    }

    private void authenticateUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String phoneNumber = user.getPhoneNumber();
            long timestamp = System.currentTimeMillis();

            if (phoneNumber != null) {
                DatabaseReference userRef = databaseReference.child(phoneNumber);
                userRef.child("authStatus").setValue("Authenticated");
                userRef.child("lastAuthenticated").setValue(timestamp);

                Log.d("FingerprintAuth", "User authentication saved in Firebase.");
                sendSuccessSMS(phoneNumber);
            }
        }
    }

    private void sendSuccessSMS(String phoneNumber) {
        Fast2SMS.sendSMS(phoneNumber, "Your fingerprint authentication was successful.");
    }
}
