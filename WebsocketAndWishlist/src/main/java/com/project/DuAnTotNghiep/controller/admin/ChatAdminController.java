package com.project.DuAnTotNghiep.controller.admin;

import com.project.DuAnTotNghiep.entity.ChatMessageEntity;
import com.project.DuAnTotNghiep.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ChatAdminController {

    @Autowired
    private ChatMessageRepository chatRepo;

    @GetMapping("/admin/chat")
    public String chatPage(Model model) {
        // ✅ Lấy toàn bộ tin nhắn, sắp xếp theo thời gian tăng dần
        List<ChatMessageEntity> messages = chatRepo.findAllOrderByCreateDateAsc();
        model.addAttribute("messages", messages);

        return "admin/chat-admin";
    }
}
