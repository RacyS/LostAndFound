package com.example.lostandfound.Model;

public class ChatMessage {
    private String senderId;
    private String receiverAdmin;
    private String content;
    private String role;
    private String Status;
    private String itemId;
    private String userId;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverAdmin() {
        return receiverAdmin;
    }

    public void setReceiverAdmin(String receiverAdmin) {
        this.receiverAdmin = receiverAdmin;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
