package com.example.lostandfound.Controller;

import com.example.lostandfound.Model.Chat;
import com.example.lostandfound.Repository.ChatMessageDto;
import com.example.lostandfound.Repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {

    @Autowired
    private ChatRepository chatRepository;

    @GetMapping("/item/{itemId}")
    public ResponseEntity<?> getChatByItemId(@PathVariable Long itemId) {
        // เปลี่ยนมาเรียกใช้เมธอดที่คุณสร้างไว้ใน Repository
        ChatMessageDto chatDetail = chatRepository.findChatDetailByItemId(itemId);

        if (chatDetail != null) {
            return ResponseEntity.ok(chatDetail);
        }
        return ResponseEntity.notFound().build();
    }
}