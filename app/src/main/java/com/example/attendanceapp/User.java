package com.example.attendanceapp;
public class User {
    public String phoneNumber;
    public String name;
    public String email;
    public String password;
    public String fingerprintKey;
    public String authStatus;
    public String lastAuthenticated;

    // Default constructor (required for Firebase)
    public User() {}

    public User(String phoneNumber, String name, String email, String password, String fingerprintKey, String authStatus, String lastAuthenticated) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.email = email;
        this.password = password;
        this.fingerprintKey = fingerprintKey;
        this.authStatus = authStatus;
        this.lastAuthenticated = lastAuthenticated;
    }
}
