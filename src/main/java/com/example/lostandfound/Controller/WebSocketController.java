package com.example.lostandfound.Controller;

import com.example.lostandfound.Model.ChatLog;
import com.example.lostandfound.Model.ChatMessage;
import com.example.lostandfound.Repository.ChatLogRepository;
import com.example.lostandfound.Service.WebSocketEventListener;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
public class WebSocketController {
    private final ChatClient chatClient;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Set<String> adminTakenOver  = new HashSet<>();
    @Autowired
    private ChatLogRepository chatLogRepository;
    public WebSocketController(ChatClient.Builder builder, SimpMessagingTemplate simpMessagingTemplate) {
        this.chatClient = builder
                .defaultSystem("""
                    คุณคือเจ้าหน้าที่รับแจ้งและตรวจสอบของหาย (Lost & Found Officer)
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
    public void sendMessageToAdmin(@Payload ChatMessage message){
        simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", message);
    }

    @MessageMapping("/chat.toUser")
    public void sendMessageToUser(@Payload ChatMessage message){
        String senderId = message.getSenderId();// เป็น student id
        String userContent = message.getContent();
        String itemId = message.getItemId();

        // หา sessionId จาก studentId
        String sessionId = WebSocketEventListener.userSessionMap.get(senderId);
        System.out.println("studentId: " + senderId + " → sessionId: " + sessionId);

        simpMessagingTemplate.convertAndSend("/topic/admin-dashboard", message);

        if (!adminTakenOver.contains(senderId) && sessionId != null) {
            String AiResponse = chatClient.prompt()
                    .user(userContent).call().content();

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

            //Log only

            Long userId = Long.parseLong(message.getUserId());
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            Long itemIdLong = Long.parseLong(itemId);
            ChatLog userLog = new ChatLog();
            userLog.setChatLog(userContent);
            userLog.setChatCreatetime(currentTime);
            userLog.setRole(1); // กำหนดตรงๆ เป็น 1 สำหรับ User
            userLog.setUserID(userId);
            System.out.println("User id"+userId);
            userLog.setTargetId(userId);
            userLog.setItemID(itemIdLong); // หรือใส่ค่าจริงที่ต้องการ
            chatLogRepository.save(userLog);

            ChatLog aiLog = new ChatLog();
            aiLog.setChatLog(AiResponse);
            aiLog.setChatCreatetime(new Timestamp(System.currentTimeMillis() + 10)); // +10ms เพื่อให้เวลาไม่ซ้ำและเรียงลำดับถูก
            aiLog.setRole(2); // กำหนดตรงๆ เป็น 2 สำหรับ AI
            aiLog.setUserID(userId);
            aiLog.setTargetId(userId);
            aiLog.setItemID(itemIdLong);
            chatLogRepository.save(aiLog);
        }
    }
    @MessageMapping("/admin.takeOver")//admin กดรับ เคส
    public void adminTakeOver(@Payload ChatMessage message){
        String targetUserId = message.getSenderId();
        adminTakenOver.add(targetUserId);

        simpMessagingTemplate.convertAndSend(
                "/topic/admin-dashboard", message
        );
        System.out.println("adminTakenOver: " + adminTakenOver);
    }

    @MessageMapping("/admin.chat.toUser")
    public void adminChatToUser(@Payload ChatMessage message){
        String targetUserId = message.getSenderId();
        String sessionId = WebSocketEventListener.userSessionMap.get(targetUserId);

        System.out.println("Admin ID"+ message.getUserId());
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

        String dbIdStr = WebSocketEventListener.userIdMap.get(targetUserId);
        Long targetDbId = Long.parseLong(dbIdStr);//id ของuser
        System.out.println("targetDbId: " + targetDbId);
        Long userId = Long.parseLong(message.getUserId()); // userId จาก DB// ของAdmin

        ChatLog adminLog = new ChatLog();
        adminLog.setChatLog(message.getContent());
        adminLog.setChatCreatetime(new Timestamp(System.currentTimeMillis()));
        adminLog.setRole(3); // 3 = Admin
        adminLog.setUserID(userId);
        adminLog.setTargetId(targetDbId);

        adminLog.setItemID(Long.parseLong(message.getItemId()));
        chatLogRepository.save(adminLog);
    }

    @GetMapping("/chat/history/{studentId}")
    public List<ChatLog> getChatHistory(@PathVariable("User_Id") Long User_Id){
        return chatLogRepository.findByUserID(User_Id);
    }
}
