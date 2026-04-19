package com.example.lostandfound.Model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "chat")
public class ChatLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logID;

    @Column(name = "chat_log")
    private String chatLog;

    @Column(name = "chat_createtime")
    private Timestamp chatCreatetime;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name ="role")
    private int role;

    public Long getLogID() {
        return logID;
    }

    public void setLogID(Long chatID) {
        this.logID = chatID;
    }

    public String getChatLog() {
        return chatLog;
    }
    public void setChatLog(String chatLog) {
        this.chatLog = chatLog;
    }

    public Timestamp getChatCreatetime() {
        return chatCreatetime;
    }

    public void setChatCreatetime(Timestamp chatCreatetime) {
        this.chatCreatetime = chatCreatetime;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long targetId) {
        this.chatId = targetId;
    }
}
