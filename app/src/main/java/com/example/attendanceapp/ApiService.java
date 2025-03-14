package com.example.attendanceapp;

import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/mark_attendance")
    Call<AttendanceResponse> markAttendance(@Body AttendanceRequest request);
    public static final String FAST2SMS_API_KEY = "DhnUQAgisc7kejplKbq5G6NJZR4H3SdBv190F8CfOuLEYWIMmVXZRDCoUYmz0PNgv1kpSbtjFaWQ2ALc";
    public static final String FAST2SMS_URL = "https://www.fast2sms.com/dev/bulkV2";

    public static void sendSMS(String phoneNumber, String message) {
        new Thread(() -> {
            try {
                String postData = "authorization=" + FAST2SMS_API_KEY +
                        "&message=" + message +
                        "&numbers=" + phoneNumber +
                        "&route=v3" +
                        "&sender_id=TXTIND";

                URL url = new URL(FAST2SMS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("Fast2SMS", "SMS sent with response code: " + responseCode);

            } catch (Exception e) {
                Log.e("Fast2SMS", "Error sending SMS: " + e.getMessage());
            }
        }).start();
    }
}