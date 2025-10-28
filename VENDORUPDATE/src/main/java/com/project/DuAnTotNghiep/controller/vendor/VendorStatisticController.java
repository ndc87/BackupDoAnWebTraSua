package com.project.DuAnTotNghiep.controller.vendor;

import com.project.DuAnTotNghiep.dto.Statistic.BestSellerProduct;
import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.repository.BillRepository;
import com.project.DuAnTotNghiep.repository.BranchRepository;
import com.project.DuAnTotNghiep.service.AccountService;
import com.project.DuAnTotNghiep.service.StatisticService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class VendorStatisticController {

    private final AccountService accountService;
    private final BillRepository billRepository;
    private final StatisticService statisticService;
    private final BranchRepository branchRepository;

    public VendorStatisticController(AccountService accountService,
                                     BillRepository billRepository,
                                     StatisticService statisticService,
                                     BranchRepository branchRepository) {
        this.accountService = accountService;
        this.billRepository = billRepository;
        this.statisticService = statisticService;
        this.branchRepository = branchRepository;
    }

    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @GetMapping("/vendor-page/thong-ke-doanh-thu")
    public String viewVendorStatisticRevenuePage(Authentication authentication, Model model) {
        // ============================================================
        // 🔍 DEBUG SECTION: Authentication Source Comparison
        // ============================================================
        String currentThread = Thread.currentThread().getName();
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 [VendorStatisticController] Method Entry Point");
        System.out.println("   📍 Path: /vendor/thong-ke-doanh-thu");
        System.out.println("   🧵 Thread: " + currentThread);
        System.out.println("=".repeat(80));

        // 1️⃣ Check Authentication injected by Spring as method parameter
        System.out.println("\n📌 SOURCE 1: Authentication injected as method parameter");
        if (authentication != null) {
            System.out.println("   ✅ [INJECTED AUTH] NOT NULL");
            System.out.println("      - Name: " + authentication.getName());
            System.out.println("      - Is Authenticated: " + authentication.isAuthenticated());
            System.out.println("      - Principal: " + authentication.getPrincipal());
            System.out.println("      - Authorities: " + authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", ")));
            System.out.println("      - Principal class: " + authentication.getPrincipal().getClass().getSimpleName());
        } else {
            System.out.println("   ❌ [INJECTED AUTH] IS NULL - Spring did not inject authentication!");
        }

        // 2️⃣ Check Authentication from SecurityContextHolder
        Authentication contextAuth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("\n📌 SOURCE 2: Authentication from SecurityContextHolder.getContext()");
        if (contextAuth != null) {
            System.out.println("   ✅ [CONTEXT AUTH] NOT NULL");
            System.out.println("      - Name: " + contextAuth.getName());
            System.out.println("      - Is Authenticated: " + contextAuth.isAuthenticated());
            System.out.println("      - Principal: " + contextAuth.getPrincipal());
            System.out.println("      - Authorities: " + contextAuth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", ")));
            System.out.println("      - Principal class: " + contextAuth.getPrincipal().getClass().getSimpleName());
        } else {
            System.out.println("   ❌ [CONTEXT AUTH] IS NULL - SecurityContext not found!");
        }

        // 3️⃣ Compare both sources
        System.out.println("\n📊 COMPARISON:");
        System.out.println("   - Injected Auth == Context Auth: " + (authentication == contextAuth));
        System.out.println("   - Injected is null: " + (authentication == null));
        System.out.println("   - Context is null: " + (contextAuth == null));
        System.out.println("=".repeat(80) + "\n");

        // ============================================================
        // 🔐 USE INJECTED AUTHENTICATION (more reliable than SecurityContextHolder)
        // ============================================================
        Authentication auth = authentication != null ? authentication : contextAuth;

        // ✅ Kiểm tra đăng nhập
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            System.out.println("⚠️ Người dùng chưa đăng nhập → redirect /user-login");
            return "redirect:/user-login";
        }

        Account account = accountService.findByEmail(auth.getName());
        if (account == null) {
            System.out.println("❌ Không tìm thấy tài khoản cho email: " + auth.getName());
            model.addAttribute("error", "Không tìm thấy tài khoản!");
            return "error";
        }

        if (account.getBranch() == null) {
            System.out.println("⚠️ Tài khoản không có chi nhánh: " + account.getEmail());
            model.addAttribute("error", "Không tìm thấy chi nhánh của tài khoản này!");
            return "error";
        }

        Long branchId = account.getBranch().getId();
        String branchName = account.getBranch().getBranchName();

        System.out.println("✅ Tài khoản: " + account.getEmail() +
                " | Chi nhánh: " + branchName + " (ID: " + branchId + ")");

        model.addAttribute("branchId", branchId);
        model.addAttribute("branchName", branchName);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime firstDayOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        LocalDateTime lastDayOfWeek = firstDayOfWeek.plusDays(6).with(LocalTime.MAX);
        LocalDateTime firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        LocalDateTime lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

        // ✅ Doanh thu hiện tại (chỉ chi nhánh của vendor)
        Double revenueAll = safeValue(billRepository.calculateTotalRevenueByBranch(branchId));
        Double revenueToday = safeValue(billRepository.calculateTotalRevenueFromDateByBranch(startOfDay, now, branchId));
        Double revenueWeek = safeValue(billRepository.calculateTotalRevenueFromDateByBranch(firstDayOfWeek, lastDayOfWeek, branchId));
        Double revenueMonth = safeValue(billRepository.calculateTotalRevenueFromDateByBranch(firstDayOfMonth, lastDayOfMonth, branchId));

        System.out.println("📊 Doanh thu hiện tại:");
        System.out.println("   - Tổng: " + revenueAll);
        System.out.println("   - Hôm nay: " + revenueToday);
        System.out.println("   - Tuần này: " + revenueWeek);
        System.out.println("   - Tháng này: " + revenueMonth);

        model.addAttribute("revenueAll", revenueAll);
        model.addAttribute("revenueToday", revenueToday);
        model.addAttribute("revenueWeek", revenueWeek);
        model.addAttribute("revenueMonth", revenueMonth);

        // ✅ Doanh thu quá khứ để tính % tăng giảm
        LocalDateTime yesterdayStart = now.minusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime yesterdayEnd = yesterdayStart.plusDays(1).minusSeconds(1);
        LocalDateTime lastWeekStart = now.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        LocalDateTime lastWeekEnd = lastWeekStart.plusDays(6).with(LocalTime.MAX);
        LocalDateTime lastMonthStart = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        LocalDateTime lastMonthEnd = lastMonthStart.plusMonths(1).minusDays(1).with(LocalTime.MAX);

        Double revenueYesterday = safeValue(billRepository.calculateTotalRevenueFromDateByBranch(yesterdayStart, yesterdayEnd, branchId));
        Double revenueLastWeek = safeValue(billRepository.calculateTotalRevenueFromDateByBranch(lastWeekStart, lastWeekEnd, branchId));
        Double revenueLastMonth = safeValue(billRepository.calculateTotalRevenueFromDateByBranch(lastMonthStart, lastMonthEnd, branchId));

        System.out.println("📈 Doanh thu quá khứ:");
        System.out.println("   - Hôm qua: " + revenueYesterday);
        System.out.println("   - Tuần trước: " + revenueLastWeek);
        System.out.println("   - Tháng trước: " + revenueLastMonth);

        // ✅ Tính % thay đổi
        model.addAttribute("percentageYesterday", calculatePercentage(revenueYesterday, revenueToday));
        model.addAttribute("percentageLastWeek", calculatePercentage(revenueLastWeek, revenueWeek));
        model.addAttribute("percentageLastMonth", calculatePercentage(revenueLastMonth, revenueMonth));

        // ✅ Top sản phẩm bán chạy của chi nhánh
        List<BestSellerProduct> bestSellers = statisticService.getBestSellerProductByBranch(branchId);
        model.addAttribute("bestSellers", bestSellers);

        System.out.println("🔥 VendorStatisticController hoàn tất xử lý và render view vendor/vendor-thong-ke-doanh-thu");

        return "vendor/vendor-thong-ke-doanh-thu";
    }

    private double calculatePercentage(double baseValue, double comparedValue) {
        if (baseValue == 0 && comparedValue > 0) return 99;
        if (baseValue == 0) return 0;
        return ((comparedValue - baseValue) / baseValue) * 100;
    }

    private Double safeValue(Double value) {
        return value == null ? 0.0 : value;
    }
}