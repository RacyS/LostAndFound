package com.example.lostandfound.Controller;

import com.example.lostandfound.Model.*;
import com.example.lostandfound.Repository.*;
import com.example.lostandfound.Service.WebSocketEventListener;
import com.google.gson.Gson;
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
    @Autowired
    private UserRepository userRepository;

    public WebSocketController(ChatClient.Builder builder, SimpMessagingTemplate simpMessagingTemplate) {
        this.chatClient = builder.build(); //
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
        System.out.println("userId ที่รับมา: " + message.getUserId());
        System.out.println("itemId ที่รับมา: " + message.getItemId());

        Optional<Chat> chatOpt = chatRepository.findByStudentFkAndItemId(userId, itemId);

        // หา Chat Session ไว้ตอบกลับ
        Chat chat = chatOpt.orElseGet(() -> {
            Chat newChat = new Chat();
            newChat.setStudentFk(userId);
            newChat.setItemId(itemId);
            newChat.setChatCreateTime(new Timestamp(System.currentTimeMillis()));
            newChat.setChatStatus("BOT_ACTIVE");
            return chatRepository.save(newChat);
        });

        // ดึงประวัติแชท
        Long chatId = chat.getChatId();
        List<ChatMessageLog> history = chatMessageLogRepository.findByChatId(chatId);
        List<Message> messages = history.stream()
                .map(log -> log.getUserId() == null
                        ? new AssistantMessage(log.getMessage())
                        : new UserMessage(log.getMessage()))
                .collect(Collectors.toList());

        String itemDetail = itemRepository.findById(itemId)
                .map(Item::getItemDetail) // ← ดึงเฉพาะ field detail
                .orElse("ไม่พบข้อมูลสิ่งของ");
        System.out.println("detail: " + itemDetail);


        String cleanContent = userContent.startsWith("#")
                ? userContent.replaceFirst("#\\S+\\s*", "").trim()
                : userContent;

        if (cleanContent.isEmpty()) {
            cleanContent = "สวัสดี";
        }


        // บันทึก log User
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        ChatMessageLog userLog = new ChatMessageLog();
        userLog.setMessage(userContent);
        userLog.setCreateAt(currentTime);
        userLog.setUserId(userId);
        userLog.setChatId(chatId);// userId ของ User
        System.out.println("chatId ก่อน save userLog: " + chatId);
        System.out.println("userLog chatId: " + userLog.getChatId()); //
        System.out.println("userLog message: " + userLog.getMessage()); //
        chatMessageLogRepository.save(userLog);
        System.out.println("chatId ที่ได้: " + chat.getChatId());

        if (chat.getChatStatus().equals("STAFF_ACTIVE")) {
            return;
        }
        if (sessionId != null) {
            // AI ตอบ
            messages.add(new UserMessage(cleanContent));
            System.out.println("cleanContent: " + cleanContent);

            String systemPrompt = """
                    คุณคือเจ้าหน้าที่รับแจ้งและตรวจสอบของหาย (Lost & Found Officer)
                    
                    [ข้อมูลสิ่งของที่พบในระบบ - ห้ามเปิดเผยให้ผู้ใช้รู้]
                    """ + itemDetail + """
                    
                    [กฎเหล็กด้านความปลอดภัย]
                    1. ห้าม! เปิดเผยรายละเอียดของสิ่งของ (สี, ขนาด, สติ๊กเกอร์, สถานที่พบ)
                    2. หน้าที่คือให้ผู้ใช้บอกรายละเอียดเอง แล้วเช็คว่า "ตรง" หรือ "ไม่ตรง"
                    3. หากบอกข้อมูลมาบางส่วน ให้ถามเจาะจงในส่วนที่ขาด
                    4. หากข้อมูลผิด ให้ตอบสุภาพว่าไม่ตรง ห้ามบอกว่าเราพบสีอะไร
                    5. หากข้อมูลครบถูกต้อง ให้มารับที่สำนักงานของหาย
                    6. เมื่อ ข้อมูลครบถ้วน ให้เริ่มต้นข้อความด้วย ข้อมูลถูกต้องครบถ้วนครับและบอกว่า รอแอดมินมายืนยัน
                    รูปแบบคำตอบ: ตอบสั้น กระชับ สุภาพ ห้ามใช้ Markdown
                    """;

            String AiResponse = chatClient.prompt()
                    .system(systemPrompt).messages(messages).call().content();
            ChatMessage replyMessage = new ChatMessage();
            replyMessage.setSenderId(senderId);
            replyMessage.setContent(AiResponse);
            replyMessage.setRole("ai");

            Boolean verified = false;
            if (AiResponse.contains("ข้อมูลถูกต้องครบถ้วนครับ")){
                verified = true;
                ChatMessage notifyMessage = new ChatMessage();
                notifyMessage.setSenderId(senderId);
                notifyMessage.setContent("");
                notifyMessage.setRole("notiverifiedfy");
                simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", notifyMessage);

                itemRepository.findById(itemId).ifPresent(item -> {
                    item.setItemStatus("UNDER_VERIFICATION");
                    itemRepository.save(item);
                });
            }

            SimpMessageHeaderAccessor headerAccessor =
                    SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessor.setSessionId(sessionId);
            headerAccessor.setLeaveMutable(true);//ให้ Spring แก้ไข header ได้ก่อนส่ง

            simpMessagingTemplate.convertAndSendToUser(
                    sessionId, "/queue/reply", replyMessage,
                    headerAccessor.getMessageHeaders()
            );
            System.out.println("replyMessage: " + replyMessage);
            simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", replyMessage);


            // บันทึก log AI
            ChatMessageLog aiLog = new ChatMessageLog();
            aiLog.setMessage(AiResponse);
            aiLog.setCreateAt(new Timestamp(System.currentTimeMillis() + 10));
            aiLog.setUserId(null);
            aiLog.setChatId(chatId);
            chatMessageLogRepository.save(aiLog);
        }
    }
    @MessageMapping("/admin.letOver")
    public void adminletOver(@Payload ChatMessage message) {
        String targetUserId = message.getSenderId();

        simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", message);

        String sessionId = WebSocketEventListener.userSessionMap.get(targetUserId);
        System.out.println("targetUserId: " + targetUserId + " sessionId: " + sessionId); // log

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

        // อัพเดต BOT_ACTIVE ใน Chat
        String userIdStr = WebSocketEventListener.userIdMap.get(targetUserId);// เอา studentId ไปหา userId จริงใน Map
        Long userUserId = userIdStr != null ? Long.parseLong(userIdStr) : null;// ถ้าหาเจอ → แปลงเป็น Long  ถ้าไม่เจอ → null

        if (userUserId != null) {
            chatRepository.findByStudentFkAndItemId(userUserId, Long.parseLong(message.getItemId()))
                    .ifPresent(chat -> {
                        chat.setChatStatus("BOT_ACTIVE");
                        chatRepository.save(chat);
                    });
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
        String userIdStr = WebSocketEventListener.userIdMap.get(targetUserId);// เอา studentId ไปหา userId จริงใน Map
        Long adminId = Long.parseLong(message.getUserId());// userId ของ Admin ที่กดรับเคส
        Long userUserId = userIdStr != null ? Long.parseLong(userIdStr) : null;// ถ้าหาเจอ → แปลงเป็น Long  ถ้าไม่เจอ → null

        if (userUserId != null) {
            chatRepository.findByStudentFkAndItemId(userUserId, Long.parseLong(message.getItemId()))
                    .ifPresent(chat -> {
                        chat.setStaffFk(adminId); // บันทึกว่า Admin ไหนรับเคส
                        chat.setChatStatus("STAFF_ACTIVE");
                        chatRepository.save(chat);
                    });
        }
    }
    @CrossOrigin(origins = "http://localhost:3000")
    @ResponseBody
    @GetMapping("/chat/status/all")
    public Map<String, String> getAllChatStatus() {
        List<Chat> allChats = chatRepository.findAll();
        Map<String, String> result = new HashMap<>();

        for (Chat chat : allChats) {
            String studentId = userRepository.findById(chat.getStudentFk())
                    .map(u -> u.getStudentId())
                    .orElse(chat.getStudentFk().toString());

            // ถ้า STAFF_ACTIVE ให้เก็บไว้
            if (chat.getChatStatus().equals("STAFF_ACTIVE")) {
                result.put(studentId, "STAFF_ACTIVE");
            }
        }
        return result;
    }

    @MessageMapping("/admin.chat.toUser")
    public void adminChatToUser(@Payload ChatMessage message) {
        String targetUserId = message.getSenderId();
        String sessionId = WebSocketEventListener.userSessionMap.get(targetUserId);
        System.out.println("itemId ที่รับมา: " + message.getItemId()); //
        System.out.println("userId ที่รับมา: " + message.getUserId()); //
        String userIdStr = WebSocketEventListener.userIdMap.get(targetUserId);
        System.out.println("userIdStr: " + userIdStr);
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
    @GetMapping("/chat/history/user/{userId}/all")
    public List<ChatMessageDto> getAllChatHistoryByUser(@PathVariable Long userId) {
        List<Chat> chats = chatRepository.findByStudentFk(userId);
        return chats.stream()
                .flatMap(c -> chatMessageLogRepository
                        .findByChatIdWithRole(c.getChatId()).stream())
                .collect(Collectors.toList());
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
            System.out.println("chatId: " + chat.getChatId() + " itemId: " + chat.getItemId());
            System.out.println("first log itemId: " + (logs.isEmpty() ? "ไม่มี" : logs.get(0).getItemId()));
            // ดึง studentId จาก User table
            String key = userRepository.findById(chat.getStudentFk())
                    .map(u -> u.getStudentId()) // ← ใช้ studentId แทน userId
                    .orElse(chat.getStudentFk().toString()); // fallback เป็น userId

            result.merge(key, logs, (a, b) -> {
                a.addAll(b);
                return a;
            });
        }
        return result;
    }
}