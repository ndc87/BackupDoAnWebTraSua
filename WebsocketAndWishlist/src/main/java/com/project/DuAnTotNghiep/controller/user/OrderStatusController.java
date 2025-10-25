package com.project.DuAnTotNghiep.controller.user;

import com.project.DuAnTotNghiep.dto.Cart.CartDto;
import com.project.DuAnTotNghiep.entity.Bill;
import com.project.DuAnTotNghiep.service.BillService;
import com.project.DuAnTotNghiep.service.CartService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Controller
public class OrderStatusController {

    private final BillService billService;
    private final CartService cartService;

    @PersistenceContext
    private EntityManager entityManager;

    public OrderStatusController(BillService billService, CartService cartService) {
        this.billService = billService;
        this.cartService = cartService;
    }

    /**
     * ✅ Hiển thị danh sách đơn hàng của người dùng
     * Luôn đảm bảo lấy dữ liệu mới nhất từ DB (không bị cache Hibernate)
     */
    @Transactional(readOnly = true)
    @GetMapping("/cart-status")
    public String viewCartStatus(Model model,
                                 @RequestParam(required = false) String status,
                                 @PageableDefault(size = 5) Pageable pageable) {

        Page<Bill> billPage;

        // ✅ Lấy danh sách bill theo tài khoản hoặc theo trạng thái
        if (status == null || status.trim().isEmpty()) {
            billPage = billService.getBillByAccount(pageable);
        } else {
            billPage = billService.getBillByStatus(status, pageable);
            model.addAttribute("status", status);
        }

        // ✅ Ép Hibernate reload dữ liệu thật từ DB để tránh cache cũ
        billPage.forEach(bill -> {
            entityManager.refresh(bill);
            System.out.println("🧾 BILL FROM DB (REFRESHED) => ID: " + bill.getId() + ", AMOUNT: " + bill.getAmount());
        });

        model.addAttribute("bills", billPage);
        return "user/cart-status";
    }

    /**
     * ✅ Hủy đơn hàng theo ID
     */
    @PostMapping("/cancel-bill/{id}")
    public String cancelBill(@PathVariable Long id) {
        billService.updateStatus("HUY", id);
        return "redirect:/cart-status";
    }

    /**
     * ✅ API lấy toàn bộ giỏ hàng của người dùng hiện tại
     */
    @ResponseBody
    @GetMapping("/api/getAllCart")
    public List<CartDto> getAllCart() {
        return cartService.getAllCartByAccountId();
    }
}
