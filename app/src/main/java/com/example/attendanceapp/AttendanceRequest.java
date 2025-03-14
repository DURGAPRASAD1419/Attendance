package com.example.attendanceapp;

public class AttendanceRequest {
    private String facultyId;
    private String timestamp;

    public AttendanceRequest(String facultyId, String timestamp) {
        this.facultyId = facultyId;
        this.timestamp = timestamp;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public String getTimestamp() {
        return timestamp;
    }
}