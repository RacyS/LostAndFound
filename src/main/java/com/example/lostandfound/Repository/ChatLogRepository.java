package com.example.lostandfound.Repository;

import com.example.lostandfound.Model.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatLogRepository extends JpaRepository <ChatLog, Long> {

    List<ChatLog> findByChatId(Long chatId);
}
