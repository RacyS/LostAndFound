package com.example.lostandfound;

public class UserLoginRequest {
    // ชื่อตัวแปรต้องตรงกับ Key ของ JSON ที่ส่งมาจาก React
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}