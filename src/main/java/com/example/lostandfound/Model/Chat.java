package com.example.lostandfound.Model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "chat")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Chat_ID")
    private Long chatId;
    @Column(name = "Chat_Createtime")
    private Timestamp chatCreateTime;
    @Column(name = "Chat_Status")
    private String chatStatus;
    @Column(name = "Student_FK")
    private Long studentFk;
    @Column(name = "Staff_FK")
    private Long staffFk;
    @Column(name = "Item_ID")
    private Long itemId;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Timestamp getChatCreateTime() {
        return chatCreateTime;
    }

    public void setChatCreateTime(Timestamp chatCreateTime) {
        this.chatCreateTime = chatCreateTime;
    }

    public String getChatStatus() {
        return chatStatus;
    }

    public void setChatStatus(String chatStatus) {
        this.chatStatus = chatStatus;
    }

    public Long getStudentFk() {
        return studentFk;
    }

    public void setStudentFk(Long studentFk) {
        this.studentFk = studentFk;
    }

    public Long getStaffFk() {
        return staffFk;
    }

    public void setStaffFk(Long staffFk) {
        this.staffFk = staffFk;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
}
