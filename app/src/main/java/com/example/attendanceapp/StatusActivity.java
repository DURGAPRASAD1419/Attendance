package com.example.attendanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StatusActivity extends AppCompatActivity {

    private TextView resultText;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        resultText = findViewById(R.id.resultText);
        backButton = findViewById(R.id.backButton);

        String facultyId = getIntent().getStringExtra("facultyId");
        sendAttendanceToServer(facultyId);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(StatusActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void sendAttendanceToServer(String facultyId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://your-aws-backend.com/") // Replace with your backend URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        AttendanceRequest request = new AttendanceRequest(facultyId, timestamp);

        apiService.markAttendance(request).enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AttendanceResponse resp = response.body();
                    if (resp.isSuccess()) {
                        resultText.setText("Attendance Verified\nSMS Sent to Faculty");
                        Toast.makeText(StatusActivity.this, "Attendance marked!", Toast.LENGTH_SHORT).show();
                    } else {
                        resultText.setText("Authentication Failed\nFaculty not registered. Contact admin.");
                        Toast.makeText(StatusActivity.this, "Not registered", Toast.LENGTH_LONG).show();
                    }
                } else {
                    resultText.setText("Server error");
                }
            }

            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                resultText.setText("Network Error: " + t.getMessage());
                Toast.makeText(StatusActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
            }
        });
    }
}