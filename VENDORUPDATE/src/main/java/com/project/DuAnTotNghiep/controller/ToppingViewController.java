package com.project.DuAnTotNghiep.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class ToppingViewController {
    
    @GetMapping("/topping-management")
    public String toppingManagementPage() {
        return "admin/topping-management";
    }
}