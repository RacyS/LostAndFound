package com.example.lostandfound.Controller;

import com.example.lostandfound.Model.*;
import com.example.lostandfound.Repository.ChatMessageLogRepository;
import com.example.lostandfound.Repository.ChatRepository;
import com.example.lostandfound.Repository.ItemRepository;
import com.example.lostandfound.Service.WebSocketEventListener;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WebSocketController {
    private final ChatClient chatClient;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private ChatMessageLogRepository chatMessageLogRepository;
    @Autowired
    private ItemRepository itemRepository;

    public WebSocketController(ChatClient.Builder builder, SimpMessagingTemplate simpMessagingTemplate) {
        this.chatClient = builder
                .defaultSystem("""
                            คุณคือเจ้าหน้าที่รับแจ้งและตรวจสอบของหาย (Lost & Found Officer)
                            ของที่เจอ
                            1.Samsung s25 ultra สีเขียว มีพวงกุญแจโดเรม่อน ภาพหน้าจอ เป็นโดเรม่อน
                        [กฎเหล็กด้านความปลอดภัย (Strict Rules) - ต้องปฏิบัติตามอย่างเคร่งครัด]
                        1. ห้าม! เปิดเผยรายละเอียดของสิ่งของใน [ข้อมูลสิ่งของที่พบแล้ว] (เช่น สี, ขนาด, สติ๊กเกอร์, สถานที่พบ) ให้ผู้ใช้รับรู้ก่อนเด็ดขาด
                        2. หน้าที่ของคุณคือให้ "ผู้ใช้เป็นคนบอกรายละเอียด" แล้วคุณมีหน้าที่แค่เช็คว่า "ตรง" หรือ "ไม่ตรง"
                        3. หากผู้ใช้บอกข้อมูลมาบางส่วน ให้ถามเจาะจงในส่วนที่ขาด เช่น "รบกวนระบุสี และจุดสังเกตพิเศษของกระเป๋าด้วยครับ" (ห้ามใบ้ว่ามีสติ๊กเกอร์)
                        4. หากผู้ใช้บอกข้อมูลผิด (เช่น บอกว่าสีแดง แต่ในระบบคือสีเงิน) ให้ตอบอย่างสุภาพว่า "ข้อมูลยังไม่ตรงกับสิ่งของที่ระบบเราพบครับ รบกวนยืนยันลักษณะหรือจุดสังเกตอื่นๆ อีกครั้ง" (ห้ามบอกเด็ดขาดว่าเราพบสีอะไร)
                        
                        รูปแบบคำตอบ:
                        -ตอบสั้น กระชับ สุภาพ
                        - ห้ามใช้เครื่องหมาย Markdown เช่น ** ในการเน้นคำ
                        """).build();
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/chat.toAdmin")
    public void sendMessageToAdmin(@Payload ChatMessage message) {
        simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", message);
    }

    @MessageMapping("/chat.toUser")
    public void sendMessageToUser(@Payload ChatMessage message) {
        String senderId = message.getSenderId();
        String userContent = message.getContent();
        Long userId = Long.parseLong(message.getUserId());
        Long itemId = Long.parseLong(message.getItemId());
        String sessionId = WebSocketEventListener.userSessionMap.get(senderId);

        simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", message);

        System.out.println("itemId ที่รับมา: " + message.getItemId());

        Optional<Chat> chatOpt = chatRepository.findByStudentFkAndItemId(userId, itemId);
        if (chatOpt.isPresent() && chatOpt.get().getChatStatus().equals("STAFF_ACTIVE")) {
            // Admin รับแล้ว ไม่ทำอะไร
            System.out.println("chatId จาก DB: " + chatOpt.get().getChatId());
            return;
        }
        // หา Chat Session ไว้ตอบกลับ
        Chat chat = chatOpt.orElseGet(() -> {
            Chat newChat = new Chat();
            newChat.setStudentFk(userId);
            newChat.setItemId(itemId);
            newChat.setChatCreateTime(new Timestamp(System.currentTimeMillis()));
            newChat.setChatStatus("BOT_ACTIVE");
            return chatRepository.save(newChat);
        });

        System.out.println("chatId ที่ได้: " + chat.getChatId());
        if (sessionId != null) {
            // AI ตอบ
            Long chatId = chat.getChatId();

            // ดึงประวัติแชท
            List<ChatMessageLog> history = chatMessageLogRepository.findByChatId(chatId);
            System.out.println("history size: " + history.size());
            List<Message> messages = history.stream()
                    .map(log -> log.getUserId()==null
                            ? new AssistantMessage(log.getMessage())
                            : new UserMessage(log.getMessage()))
                    .collect(Collectors.toList());
            System.out.println("messages size: " + messages.size());

            String itemDetail = itemRepository.findById(itemId)
                    .map(Item::getItemDetail) // ← ดึงเฉพาะ field detail
                    .orElse("ไม่พบข้อมูลสิ่งของ");
            System.out.println("detail: " + itemDetail);


            String cleanContent = userContent.startsWith("#")
                    ? userContent.replaceFirst("#\\S+\\s*", "").trim()
                    : userContent;
            messages.add(new UserMessage(cleanContent));

            String AiResponse = chatClient.prompt()
                    .messages(messages).call().content();
            ChatMessage replyMessage = new ChatMessage();
            replyMessage.setSenderId(senderId);
            replyMessage.setContent(AiResponse);
            replyMessage.setRole("ai");

            SimpMessageHeaderAccessor headerAccessor =
                    SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessor.setSessionId(sessionId);
            headerAccessor.setLeaveMutable(true);

            simpMessagingTemplate.convertAndSendToUser(
                    sessionId, "/queue/reply", replyMessage,
                    headerAccessor.getMessageHeaders()
            );
            simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", replyMessage);

            System.out.println("chatId ก่อน save userLog: " + chatId);
            // บันทึก log User
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            ChatMessageLog userLog = new ChatMessageLog();
            userLog.setMessage(userContent);
            userLog.setCreateAt(currentTime);
            userLog.setUserId(userId);
            userLog.setChatId(chatId);// userId ของ User

            System.out.println("userLog chatId: " + userLog.getChatId()); //
            System.out.println("userLog message: " + userLog.getMessage()); //
            chatMessageLogRepository.save(userLog);

            // บันทึก log AI
            ChatMessageLog aiLog = new ChatMessageLog();
            aiLog.setMessage(AiResponse);
            aiLog.setCreateAt(new Timestamp(System.currentTimeMillis() + 10));
            aiLog.setUserId(null);
            aiLog.setChatId(chatId);
            chatMessageLogRepository.save(aiLog);
        }
    }

    @MessageMapping("/admin.takeOver")
    public void adminTakeOver(@Payload ChatMessage message) {
        String targetUserId = message.getSenderId();

        simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", message);

        String sessionId = WebSocketEventListener.userSessionMap.get(targetUserId);
        System.out.println("targetUserId: " + targetUserId + " sessionId: " + sessionId); // log ดูก่อน

        // เช็คก่อนส่ง
        if (sessionId == null) {
            System.out.println("ไม่พบ sessionId ของ: " + targetUserId);
            return;
        }

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);

        simpMessagingTemplate.convertAndSendToUser(
                sessionId, "/queue/reply", message,
                headerAccessor.getMessageHeaders()
        );

        // อัพเดต Staff_FK ใน Chat
        String userIdStr = WebSocketEventListener.userIdMap.get(targetUserId);
        Long adminId = Long.parseLong(message.getUserId());
        Long userUserId = userIdStr != null ? Long.parseLong(userIdStr) : null;

        if (userUserId != null) {
            chatRepository.findByStudentFkAndItemId(userUserId, Long.parseLong(message.getItemId()))
                    .ifPresent(chat -> {
                        chat.setStaffFk(adminId); // บันทึกว่า Admin ไหนรับเคส
                        chat.setChatStatus("STAFF_ACTIVE");
                        chatRepository.save(chat);
                    });
        }
    }

    @MessageMapping("/admin.chat.toUser")
    public void adminChatToUser(@Payload ChatMessage message) {
        String targetUserId = message.getSenderId();
        String sessionId = WebSocketEventListener.userSessionMap.get(targetUserId);

        String userIdStr = WebSocketEventListener.userIdMap.get(targetUserId);
        if (userIdStr == null) return;

        Long userId = Long.parseLong(userIdStr);
        Long itemId = Long.parseLong(message.getItemId());
        Long adminId = Long.parseLong(message.getUserId());

        message.setRole("admin");
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);

        simpMessagingTemplate.convertAndSendToUser(
                sessionId, "/queue/reply", message,
                headerAccessor.getMessageHeaders()
        );
        simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", message);

        chatRepository.findByStudentFkAndItemId(userId, itemId)
                .ifPresent(chat -> {
                    ChatMessageLog adminLog = new ChatMessageLog();
                    adminLog.setChatId(chat.getChatId());
                    adminLog.setMessage(message.getContent());
                    adminLog.setCreateAt(new Timestamp(System.currentTimeMillis()));
                    adminLog.setUserId(adminId); // userId ของ Admin
                    chatMessageLogRepository.save(adminLog);
                });
    }
    @CrossOrigin(origins = "http://localhost:3000")
    @ResponseBody
    @GetMapping("/chat/history/user/{userId}/item/{itemId}")
    public List<ChatMessageDto> getChatHistory(
            @PathVariable Long userId,
            @PathVariable Long itemId) {
        System.out.println("หา history userId: " + userId + " itemId: " + itemId);
        Optional<Chat> chat = chatRepository.findByStudentFkAndItemId(userId, itemId);
        System.out.println("เจอ chat ไหม: " + chat.isPresent());
        if (chat.isEmpty()) return List.of();
        System.out.println("chatId: " + chat.get().getChatId());
        List<ChatMessageDto> result = chatMessageLogRepository.findByChatIdWithRole(chat.get().getChatId());
        System.out.println("จำนวน message: " + result.size());

        return chatMessageLogRepository.findByChatIdWithRole(chat.get().getChatId());
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @ResponseBody
    @GetMapping("/chat/history/all")
    public Map<String, List<ChatMessageDto>> getAllChatHistory() {
        List<Chat> allChats = chatRepository.findAll();
        Map<String, List<ChatMessageDto>> result = new HashMap<>();
        for (Chat chat : allChats) {
            List<ChatMessageDto> logs = chatMessageLogRepository
                    .findByChatIdWithRole(chat.getChatId());
            String key = chat.getStudentFk().toString();
            result.merge(key, logs, (a, b) -> { a.addAll(b); return a; });
        }
        return result;
    }
}
