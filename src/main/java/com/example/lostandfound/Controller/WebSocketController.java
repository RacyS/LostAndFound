package com.example.lostandfound.Controller;

import com.example.lostandfound.Model.ChatLog;
import com.example.lostandfound.Model.ChatMessage;
import com.example.lostandfound.Model.ChatSession;
import com.example.lostandfound.Repository.ChatLogRepository;
import com.example.lostandfound.Repository.ChatSessionRepository;
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
    private final Set<String> adminTakenOver = new HashSet<>();
    @Autowired
    private ChatLogRepository chatLogRepository;
    @Autowired
    private ChatSessionRepository chatSessionRepository;

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

        String senderId = message.getSenderId();// เป็น student id
        String userContent = message.getContent();
        Long userId = Long.parseLong(message.getUserId());
        Long itemId = Long.parseLong(message.getItemId());


                // หา sessionId จาก studentId
        String sessionId = WebSocketEventListener.userSessionMap.get(senderId);
        System.out.println("studentId: " + senderId + " → sessionId: " + sessionId);

        simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", message);

        if (!adminTakenOver.contains(senderId) && sessionId != null) {
            ChatSession chatSession = chatSessionRepository.findByUserIDAndItemID(userId, itemId)
                    .orElseGet(() -> {
                        ChatSession newSession = new ChatSession();
                        newSession.setUserID(userId);
                        newSession.setItemID(itemId);
                        newSession.setStudentId(senderId);
                        newSession.setChatStartTime(new Timestamp(System.currentTimeMillis()));
                        return chatSessionRepository.save(newSession);
                    });
            Long chatId = chatSession.getChatId();

            List<ChatLog> history = chatLogRepository.findByChatId(chatId);
            List<Message> messages  = history.stream() // สร้างประวัติแชททั้งหมดจาก
                    .map(log -> log.getRole()==1
                            ? new UserMessage(log.getChatLog())
                            : new AssistantMessage(log.getChatLog()))
                    .collect(Collectors.toList());

            messages.add(new UserMessage(userContent)); //เพิ่มข้อความล่าสุด

            String AiResponse = chatClient.prompt()
                    .messages(messages).call().content();
            ChatMessage replyMessage = new ChatMessage();
            replyMessage.setSenderId(senderId);
            replyMessage.setContent(AiResponse);
            replyMessage.setRole("ai");

            SimpMessageHeaderAccessor headerAccessor =
                    SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessor.setSessionId(sessionId);//ส่งไปยัง session ที่เลือก
            headerAccessor.setLeaveMutable(true);

            simpMessagingTemplate.convertAndSendToUser(
                    sessionId, "/queue/reply", replyMessage,
                    headerAccessor.getMessageHeaders()
            );
            simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", replyMessage);

            //Log only
            //ChatLog
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            ChatLog userLog = new ChatLog();
            userLog.setChatId(chatId);
            userLog.setChatLog(userContent);
            userLog.setChatCreatetime(currentTime);
            userLog.setRole(1); // กำหนดตรงๆ เป็น 1 สำหรับ User
            chatLogRepository.save(userLog);

            ChatLog aiLog = new ChatLog();
            aiLog.setChatId(chatId);
            aiLog.setChatLog(AiResponse);
            aiLog.setChatCreatetime(currentTime);
            aiLog.setRole(2); // กำหนดตรงๆ เป็น 1 สำหรับ User
            chatLogRepository.save(aiLog);
        }
    }

    @MessageMapping("/admin.takeOver")//admin กดรับ เคส
    public void adminTakeOver(@Payload ChatMessage message) {
        String targetUserId = message.getSenderId();

        adminTakenOver.add(targetUserId);

        simpMessagingTemplate.convertAndSend(
                "/topic/admin-dashboard", message
        );
        String sessionId = WebSocketEventListener.userSessionMap.get(targetUserId);
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId); // ใช้ sessionId
        headerAccessor.setLeaveMutable(true);

        simpMessagingTemplate.convertAndSendToUser(
                sessionId, "/queue/reply", message,
                headerAccessor.getMessageHeaders()
        );
        System.out.println("adminTakenOver: " + adminTakenOver);
    }
    @CrossOrigin(origins = "http://localhost:3000")
    @ResponseBody
    @GetMapping("/admin/takenover")
    public Set<String> getTakenOver() {
        return adminTakenOver;
    }

    @MessageMapping("/admin.chat.toUser")
    public void adminChatToUser(@Payload ChatMessage message) {
        String targetUserId = message.getSenderId();
        String sessionId = WebSocketEventListener.userSessionMap.get(targetUserId);

        String userIdStr = WebSocketEventListener.userIdMap.get(targetUserId);
        if (userIdStr == null) {
            System.out.println("ไม่พบ userId ของ: " + targetUserId);
            return;
        }

        Long userId = Long.parseLong(userIdStr);
        Long itemId = Long.parseLong(message.getItemId());
        System.out.println("Admin ID" + message.getUserId());
        System.out.println("role ที่รับมา: " + message.getRole());
        System.out.println("Admin ส่งหา studentId: " + targetUserId);
        System.out.println("sessionId ที่หาได้: " + sessionId);
        System.out.println("content ที่จะส่ง: " + message.getContent()); // เพิ่มตรงนี้
        System.out.println("role ที่จะส่ง: " + message.getRole());

        message.setRole("admin");
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);


        simpMessagingTemplate.convertAndSendToUser(
                sessionId, "/queue/reply", message,
                headerAccessor.getMessageHeaders()
        );
        simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", message);

        ChatSession chatSession = chatSessionRepository
                .findByUserIDAndItemID(userId, itemId)
                .orElse(null);

        if (chatSession != null) {
            ChatLog adminLog = new ChatLog();
            adminLog.setChatId(chatSession.getChatId());
            adminLog.setChatLog(message.getContent());
            adminLog.setChatCreatetime(new Timestamp(System.currentTimeMillis()));
            adminLog.setRole(3);
            chatLogRepository.save(adminLog);
        } else {
            System.out.println("ไม่พบ Session ของ userId: " + userId + " itemId: " + itemId);
        }
    }
    // มาอ่านอีกทีให้เข้าใจ
    @CrossOrigin(origins = "http://localhost:3000")
    @ResponseBody
    @GetMapping("/chat/history/user/{userId}")
    public List<ChatLog> getChatHistory(@PathVariable Long userId) {
        List<ChatSession> sessions = chatSessionRepository.findByUserID(userId);
        return sessions.stream()
                .flatMap(s -> chatLogRepository.findByChatId(s.getChatId()).stream())
                .collect(Collectors.toList());
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @ResponseBody
    @GetMapping("/chat/history/all")
    public Map<String, List<ChatLog>> getAllChatHistory() {
        List<ChatSession> allSessions = chatSessionRepository.findAll();

        Map<String, List<ChatLog>> result = new HashMap<>();
        for (ChatSession session : allSessions) {
            List<ChatLog> logs = chatLogRepository.findByChatId(session.getChatId());
            String key = session.getStudentId(); // ใช้ studentId เป็น key
            result.merge(key, logs, (a, b) -> {
                a.addAll(b);
                return a;
            });
        }
        return result;
    }
}
