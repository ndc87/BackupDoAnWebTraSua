package com.project.DuAnTotNghiep.controller.api;

import com.project.DuAnTotNghiep.service.WishlistService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistRestController {

    private final WishlistService wishlistService;

    public WishlistRestController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/count/{accountId}")
    public int countWishlist(@PathVariable Long accountId) {
        return wishlistService.countWishlistItems(accountId);
    }
}
