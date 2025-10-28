package com.project.DuAnTotNghiep.controller.vendor;

import com.project.DuAnTotNghiep.entity.Branch;
import com.project.DuAnTotNghiep.repository.BranchRepository;
import com.project.DuAnTotNghiep.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vendor-page")
public class VendorProductViewController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BranchRepository branchRepository;

    @GetMapping("/product-all")
    public String showProductList(Model model, Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        String email = authentication.getName();

        // 🔹 Lấy branch_id theo account
        Long branchId = accountRepository.findBranchIdByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy branch_id cho vendor: " + email));

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh ID: " + branchId));

        model.addAttribute("branch", branch);
        return "vendor/product-all";
    }

    @GetMapping("/product-detail")
    public String showProductDetail(Model model, Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        String email = authentication.getName();

        Long branchId = accountRepository.findBranchIdByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy branch_id cho vendor: " + email));

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh ID: " + branchId));

        model.addAttribute("branch", branch);
        return "vendor/product-detail";
    }
}
