package com.project.DuAnTotNghiep.websocket;

import com.project.DuAnTotNghiep.entity.ChatMessageEntity;
import com.project.DuAnTotNghiep.repository.ChatMessageRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatRepo;
    private static final String ADMIN_EMAIL = "admin@gmail.com";

    public ChatController(SimpMessagingTemplate messagingTemplate, ChatMessageRepository chatRepo) {
        this.messagingTemplate = messagingTemplate;
        this.chatRepo = chatRepo;
    }

    @MessageMapping("/chat.send")
    public void handleMessage(@Payload ChatMessage message, Principal principal) {
        System.out.println("🔥 Received message: " + message.getFrom() + " -> " + message.getTo() + ": " + message.getContent());
        
        if (message.getFrom() == null || message.getFrom().isBlank()) {
            message.setFrom(principal != null ? principal.getName() : "guest");
        }

        // ✅ Nếu gửi đến admin -> normalize email
        String receiver = message.getTo();
        if (receiver != null && receiver.equalsIgnoreCase("admin")) {
            receiver = ADMIN_EMAIL;
            message.setTo(ADMIN_EMAIL);
        }

        // 🔑 Tạo roomId theo cặp người chat
        String a = message.getFrom();
        String b = (receiver != null && !receiver.isBlank()) ? receiver : ADMIN_EMAIL;
        String roomId = buildRoomId(a, b);

        // ✅ Lưu vào DB với try-catch để debug
        try {
            ChatMessageEntity entity = new ChatMessageEntity();
            entity.setSender(message.getFrom());
            entity.setContent(message.getContent());
            entity.setRoomId(roomId);
            entity.setSeen(false);
            entity.setCreateDate(LocalDateTime.now());
            entity.setCreatedAt(LocalDateTime.now());
            
            ChatMessageEntity saved = chatRepo.save(entity);
            System.out.println("✅ Message saved to DB with ID: " + saved.getId() + ", RoomId: " + roomId);
        } catch (Exception e) {
            System.err.println("❌ Error saving message to DB: " + e.getMessage());
            e.printStackTrace();
        }

        // ✅ Gửi realtime
        if (receiver != null && !receiver.isBlank()) {
            messagingTemplate.convertAndSendToUser(receiver, "/queue/messages", message);
            System.out.println("📤 Sent private to: " + receiver);
        } else {
            messagingTemplate.convertAndSend("/topic/public", message);
            System.out.println("📢 Sent public message");
        }
    }

    private String buildRoomId(String u1, String u2) {
        String a = u1 == null ? "" : u1.trim().toLowerCase();
        String b = u2 == null ? "" : u2.trim().toLowerCase();
        return (a.compareTo(b) <= 0) ? a + "|" + b : b + "|" + a;
    }
}