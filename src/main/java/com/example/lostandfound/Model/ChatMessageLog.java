package com.example.lostandfound.Model;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "chat_message")
public class ChatMessageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Message_ID")
    private Long messageId;
    @Column(name = "Message")
    private String message;
    @Column(name = "Create_at")
    private Timestamp createAt;
    @Column(name = "User_ID")
    private Long  userId;
    @Column(name = "Chat_ID")
    private Long chatId;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }
}
