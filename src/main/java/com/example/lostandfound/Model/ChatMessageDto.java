package com.example.lostandfound.Model;

import java.sql.Timestamp;

public interface ChatMessageDto {
    Long getMessageId();
    String getMessage();
    Timestamp getCreateAt();
    Long getUserId();
    Long getChatId();
    String getRole();
}