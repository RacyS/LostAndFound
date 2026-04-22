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
}
