package com.example.lostandfound.Repository;

import com.example.lostandfound.Model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findByUserIDAndItemID(Long userID, Long itemID);

    List<ChatSession> findByUserID(Long userID); // เพิ่มตรงนี้

}
