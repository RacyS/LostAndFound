package com.example.lostandfound.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "User_ID")
    Long userID;

    @Column(name = "User_Email")
    String userEmail;

    @Column(name = "User_Password")
    String userPassword;

    @Column(name = "User_Name")
    String userName;

    @Column(name = "Student_ID")
    String studentID;
    @Column(name = "Staff_ID")
    String staffID;
    int role;

    public int getRole() {
        return role;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getStaffID() {
        return staffID;
    }

    public void setStaffID(String staffID) {
        this.staffID = staffID;
    }

    public void setRole(int role) {
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

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String student_id) {
        this.studentID = student_id;
    }

}
