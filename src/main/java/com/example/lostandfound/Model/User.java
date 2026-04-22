package com.example.lostandfound.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "User_ID")
    Long userId;

    @Column(name = "User_Name")
    String userName;

    @Column(name = "User_Email")
    String userEmail;

    @Column(name = "User_Password")
    String userPassword;

    @Column(name = "Student_ID")
    String studentId;
    @Column(name = "Staff_ID")
    String staffId;
    String role;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userID) {
        this.userId = userID;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffID) {
        this.staffId = staffID;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String password) {
        this.userPassword = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String student_name) {
        this.userName = student_name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String student_id) {
        this.studentId = student_id;
    }

}
