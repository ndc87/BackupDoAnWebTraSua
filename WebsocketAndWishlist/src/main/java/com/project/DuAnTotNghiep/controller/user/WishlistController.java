package com.project.DuAnTotNghiep.controller.user;

import com.project.DuAnTotNghiep.service.WishlistService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public String showWishlist(Model model) {
        // ⚠️ Tạm thời fix cứng accountId = 1, lát sẽ thay bằng id user đăng nhập
        Long accountId = 1L;
        model.addAttribute("items", wishlistService.getWishlistItems(accountId));
        return "user/wishlist"; // Trỏ tới file src/main/resources/templates/user/wishlist.html
    }
}
