package com.example.lostandfound.Repository;

import com.example.lostandfound.Model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByStudentFkAndItemId(Long studentFk, Long itemId);
    List<Chat> findByStudentFk(Long studentFk);
}
