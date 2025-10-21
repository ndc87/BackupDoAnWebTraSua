package com.project.DuAnTotNghiep.controller.admin;

import com.project.DuAnTotNghiep.entity.ChatMessageEntity;
import com.project.DuAnTotNghiep.repository.ChatMessageRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatDebugController {

    private final ChatMessageRepository chatRepo;

    public ChatDebugController(ChatMessageRepository chatRepo) {
        this.chatRepo = chatRepo;
    }

    /**
     * ✅ API debug: Xem tất cả tin nhắn trong DB
     * Endpoint: /admin/api/chat/debug
     */
    @GetMapping("/admin/api/chat/debug")
    public List<ChatMessageEntity> getAllMessages() {
        List<ChatMessageEntity> messages = chatRepo.findAll();
        System.out.println("🔍 Total messages in DB: " + messages.size());
        return messages;
    }

    /**
     * ✅ API debug: Xem số lượng tin nhắn
     * Endpoint: /admin/api/chat/count
     */
    @GetMapping("/admin/api/chat/count")
    public String getMessageCount() {
        long count = chatRepo.count();
        return "Total messages in database: " + count;
    }
}