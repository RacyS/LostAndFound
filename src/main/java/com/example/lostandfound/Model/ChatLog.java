package com.example.lostandfound.Model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "chat")
public class ChatLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long chatID;

    @Column(name = "chat_log")
    private String chatLog;

    @Column(name = "chat_createtime")
    private Timestamp chatCreatetime;

    @Column(name = "User_ID")
    private Long userID;

    @Column(name = "Tar_get_User_Id")
    private Long targetId;

    @Column(name = "Item_ID")
    private Long itemID;

    @Column(name ="role")
    private int role;

    public Long getChatID() {
        return chatID;
    }

    public void setChatID(Long chatID) {
        this.chatID = chatID;
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

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public Long getItemID() {
        return itemID;
    }

    public void setItemID(Long itemID) {
        this.itemID = itemID;
    }

    public int isRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
}
