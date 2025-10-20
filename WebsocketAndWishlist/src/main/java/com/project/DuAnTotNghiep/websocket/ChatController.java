package com.project.DuAnTotNghiep.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void handleMessage(@Payload ChatMessage message, Principal principal) {
        if (message.getFrom() == null || message.getFrom().isBlank()) {
            message.setFrom(principal != null ? principal.getName() : "guest");
        }

        System.out.println("📩 Message from: " + message.getFrom() + " → to: " + message.getTo());
        System.out.println("👤 Principal: " + (principal != null ? principal.getName() : "null"));

        // ✅ Nếu gửi riêng (to != null)
        if (message.getTo() != null && !message.getTo().isBlank()) {
            String receiver = message.getTo();

            // 🔒 Trường hợp người gửi là user, mà to là admin → ép về email admin
            if (receiver.equalsIgnoreCase("admin")) {
                receiver = "admin@gmail.com";
            }

            messagingTemplate.convertAndSendToUser(receiver, "/queue/messages", message);
            System.out.println("📤 Sent private to: " + receiver);
        } else {
            // ✅ Nếu gửi công khai
            messagingTemplate.convertAndSend("/topic/public", message);
            System.out.println("📢 Sent public message");
        }
    }
}
