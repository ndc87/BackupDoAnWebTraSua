package com.project.DuAnTotNghiep.controller.api;

import com.project.DuAnTotNghiep.dto.CheckOrderDto;
import com.project.DuAnTotNghiep.dto.Order.OrderDto;
import com.project.DuAnTotNghiep.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class OrderController {
    private final CartService cartService;
    private final ObjectMapper objectMapper;

    public OrderController(CartService cartService, ObjectMapper objectMapper) {
        this.cartService = cartService;
        this.objectMapper = objectMapper;
    }

    @ResponseBody
    @PostMapping("/api/orderUser")
    public void order(@RequestBody OrderDto orderDto) {
        cartService.orderUser(orderDto);
    }

    @ResponseBody
    @PostMapping("/api/orderAdmin")
    public OrderDto orderAdmin(@RequestBody OrderDto orderDto) {
        return cartService.orderAdmin(orderDto);
    }

    @ResponseBody
    @PostMapping("/api/checkOrder")
    public List<CheckOrderDto> checkOrder(@RequestBody List<CheckOrderDto> checkOrderDtoList) {
        return null;
    }

    // ✅ API để lưu đơn hàng tạm thời vào session trước khi thanh toán
    @ResponseBody
    @PostMapping("/api/save-order-temp")
    public void saveOrderTemp(@RequestBody OrderDto orderDto, HttpSession session) {
        try {
            // ✅ Convert OrderDto thành JSON string và lưu vào session
            String orderJson = objectMapper.writeValueAsString(orderDto);
            session.setAttribute("orderTemp", orderJson);
            System.out.println("✅ Lưu đơn hàng tạm thời vào session thành công");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Lỗi khi lưu đơn hàng tạm thời: " + e.getMessage());
        }
    }
}