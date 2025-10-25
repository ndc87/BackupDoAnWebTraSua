// Đường dẫn: WebsocketAndWishlist/src/main/java/com/project/DuAnTotNghiep/controller/UserToppingController.java

package com.project.DuAnTotNghiep.controller;

import com.project.DuAnTotNghiep.dto.Topping.ToppingDto;
import com.project.DuAnTotNghiep.service.ToppingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/toppings")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class UserToppingController {
    
    @Autowired
    private ToppingService toppingService;
    
    /**
     * Lấy danh sách topping đang hoạt động (cho user chọn trong giỏ hàng)
     */
    @GetMapping
    public ResponseEntity<List<ToppingDto>> getActiveToppings() {
        return ResponseEntity.ok(toppingService.getActiveToppings());
    }
    
    /**
     * Lấy thông tin chi tiết 1 topping theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ToppingDto> getToppingById(@PathVariable Long id) {
        return ResponseEntity.ok(toppingService.getToppingById(id));
    }
}