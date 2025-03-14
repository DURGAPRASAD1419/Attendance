package com.example.attendanceapp;

import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Fast2SMS extends AppCompatActivity {
    private static final String API_KEY = "DhnUQAgisc7kejplKbq5G6NJZR4H3SdBv190F8CfOuLEYWIMmVXZRDCoUYmz0PNgv1kpSbtjFaWQ2ALc";

    public static void sendSMS(String phoneNumber, String message) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    String url = "https://www.fast2sms.com/dev/bulkV2?authorization=" + API_KEY
                            + "&route=v3&sender_id=TXTIND&message=" + URLEncoder.encode(message, "UTF-8")
                            + "&language=english&flash=0&numbers=" + phoneNumber;

                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Cache-Control", "no-cache");

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        Log.d("Fast2SMS", "SMS Sent: " + response.toString());
                        return response.toString();
                    } else {
                        Log.e("Fast2SMS", "Failed to send SMS: Response Code " + responseCode);
                    }
                } catch (Exception e) {
                    Log.e("Fast2SMS", "Error sending SMS: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }
}
