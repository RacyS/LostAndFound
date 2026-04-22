package com.example.lostandfound.Repository;

import com.example.lostandfound.Model.ChatMessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageLogRepository extends JpaRepository<ChatMessageLog, Long> {
    List<ChatMessageLog> findByChatId(Long chatId);
    @Query(value = "SELECT m.Message_ID as messageId, m.Message as message, " +
            "m.Create_at as createAt, m.User_ID as userId, m.Chat_ID as chatId, " +
            "c.Item_ID as itemId, " + // ← เพิ่มตรงนี้
            "CASE WHEN m.User_ID IS NULL THEN 'ai' " +
            "     WHEN u.Role = 'STAFF' THEN 'admin' " +
            "     ELSE 'user' END as role " +
            "FROM chat_message m " +
            "LEFT JOIN user u ON m.User_ID = u.User_ID " +
            "LEFT JOIN chat c ON m.Chat_ID = c.Chat_ID " + // ← JOIN chat ด้วย
            "WHERE m.Chat_ID = :chatId " +
            "ORDER BY m.Create_at ASC",
            nativeQuery = true)
    List<ChatMessageDto> findByChatIdWithRole(@Param("chatId") Long chatId);

}
