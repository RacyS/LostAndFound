package com.example.lostandfound.Service;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {

    // สมุดโทรศัพท์: studentId → sessionId
    public static final Map<String, String> userSessionMap = new ConcurrentHashMap<>();
    public static final Map<String, String> userIdMap = new ConcurrentHashMap<>();
    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        // wrap = ห่อ message ด้วย accessor เพื่อให้ดึง header ได้ง่าย
        String sessionId = accessor.getSessionId();
        String studentId = accessor.getFirstNativeHeader("studentId");
        String userId = accessor.getFirstNativeHeader("userId");
        if (studentId != null) {
            userSessionMap.put(studentId, sessionId);
            System.out.println("ผูก: " + studentId + " → " + sessionId);
        }

        if (studentId != null && userId != null) {
            userIdMap.put(studentId, userId); // เพิ่มตรงนี้
            System.out.println("ผูก userId: " + studentId + " → " + userId);
        }
    }
}