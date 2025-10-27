package com.project.DuAnTotNghiep.controller.admin;

import com.project.DuAnTotNghiep.dto.Statistic.BestSellerProduct;
import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.repository.BillRepository;
import com.project.DuAnTotNghiep.repository.BranchRepository;
import com.project.DuAnTotNghiep.service.AccountService;
import com.project.DuAnTotNghiep.service.StatisticService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Controller
@PreAuthorize("hasRole('VENDOR')")
public class VendorStatisticController {

    private final AccountService accountService;
    private final BillRepository billRepository;
    private final StatisticService statisticService;
    private final BranchRepository branchRepository;

    public VendorStatisticController(AccountService accountService, BillRepository billRepository,
                                     StatisticService statisticService, BranchRepository branchRepository) {
        this.accountService = accountService;
        this.billRepository = billRepository;
        this.statisticService = statisticService;
        this.branchRepository = branchRepository;
    }

    @GetMapping("/vendor/thong-ke-doanh-thu")
    public String viewVendorStatisticRevenuePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountService.findByEmail(auth.getName());

        if (account == null || account.getBranch() == null) {
            model.addAttribute("error", "Không tìm thấy chi nhánh của tài khoản này!");
            return "error";
        }

        Long branchId = account.getBranch().getId();
        String branchName = account.getBranch().getBranchName();

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

        // ✅ Tính % thay đổi
        model.addAttribute("percentageYesterday", calculatePercentage(revenueYesterday, revenueToday));
        model.addAttribute("percentageLastWeek", calculatePercentage(revenueLastWeek, revenueWeek));
        model.addAttribute("percentageLastMonth", calculatePercentage(revenueLastMonth, revenueMonth));

        // ✅ Top sản phẩm bán chạy của chi nhánh
        List<BestSellerProduct> bestSellers = statisticService.getBestSellerProductByBranch(branchId);
        model.addAttribute("bestSellers", bestSellers);

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
