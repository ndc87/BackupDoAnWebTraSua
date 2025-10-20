package com.project.DuAnTotNghiep.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatAdminController {

    @GetMapping("/admin/chat")
    public String chatPage() {
        return "admin/chat-admin";
    }
}
