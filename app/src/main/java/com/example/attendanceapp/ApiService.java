package com.example.attendanceapp;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/mark_attendance")
    Call<AttendanceResponse> markAttendance(@Body AttendanceRequest request);
}
