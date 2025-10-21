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

    public ChatController(SimpMessagingTemplate messagingTemplate, ChatMessageRepository chatRepo) {
        this.messagingTemplate = messagingTemplate;
        this.chatRepo = chatRepo;
    }

    @MessageMapping("/chat.send")
    public void handleMessage(@Payload ChatMessage message, Principal principal) {
        // ✅ Nếu from chưa có thì gán theo người đăng nhập
        if (message.getFrom() == null || message.getFrom().isBlank()) {
            message.setFrom(principal != null ? principal.getName() : "guest");
        }

        System.out.println("📩 Message from: " + message.getFrom() + " → to: " + message.getTo());
        System.out.println("👤 Principal: " + (principal != null ? principal.getName() : "null"));

        // ✅ Lưu tin nhắn vào DB
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setSender(message.getFrom());
        entity.setContent(message.getContent());
        entity.setReceiverId(null); // nếu sau này có accountId thì map thêm
        entity.setRoomId("default"); // có thể tách room riêng từng user
        entity.setSeen(false);
        entity.setCreateDate(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());
        chatRepo.save(entity);

        // ✅ Gửi tin nhắn real-time
        if (message.getTo() != null && !message.getTo().isBlank()) {
            String receiver = message.getTo();

            // ép admin name → email admin
            if (receiver.equalsIgnoreCase("admin")) {
                receiver = "admin@gmail.com";
            }

            messagingTemplate.convertAndSendToUser(receiver, "/queue/messages", message);
            System.out.println("📤 Sent private to: " + receiver);
        } else {
            messagingTemplate.convertAndSend("/topic/public", message);
            System.out.println("📢 Sent public message");
        }
    }
}
