package com.example.lostandfound.Repository;

import com.example.lostandfound.Model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByStudentFkAndItemId(Long studentFk, Long itemId);
    List<Chat> findByStudentFk(Long studentFk);
    @Query("SELECT c FROM Chat c WHERE c.studentFk = :studentFk ORDER BY c.chatId DESC")
    List<Chat> findByStudentFkOrderByDesc(@Param("studentFk") Long studentFk);

    @Query(value = "SELECT c.Chat_ID as chatId, u.student_id as studentId, c.Student_FK as studentDbId " +
            "FROM chat c " +
            "JOIN user u ON c.Student_FK = u.User_ID " +
            "WHERE c.Item_ID = :itemId LIMIT 1", nativeQuery = true)
    ChatMessageDto findChatDetailByItemId(@Param("itemId") Long itemId);
}
