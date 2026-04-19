package com.example.lostandfound.Model;

import jakarta.persistence.*;

import java.sql.Timestamp;
@Entity
@Table(name = "chatsession")public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    @Column(name = "User_id")
    Long userID;

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "Item_ID")
    private Long itemID;

    @Column(name = "ChatStartTime")
    private Timestamp chatStartTime;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getStudentId() {
        return studentId;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Long getItemID() {
        return itemID;
    }

    public void setItemID(Long itemID) {
        this.itemID = itemID;
    }

    public Timestamp getChatStartTime() {
        return chatStartTime;
    }

    public void setChatStartTime(Timestamp chatStartTime) {
        this.chatStartTime = chatStartTime;
    }
}
