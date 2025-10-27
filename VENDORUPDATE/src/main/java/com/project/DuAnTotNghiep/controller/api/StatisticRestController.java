package com.project.DuAnTotNghiep.controller.api;

import com.project.DuAnTotNghiep.dto.Statistic.*;
import com.project.DuAnTotNghiep.service.AccountService;
import com.project.DuAnTotNghiep.service.StatisticService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StatisticRestController {

    private final StatisticService statisticService;
    private final AccountService accountService;

    public StatisticRestController(StatisticService statisticService, AccountService accountService) {
        this.statisticService = statisticService;
        this.accountService = accountService;
    }

    // =============================================================
    // 🟩 1. Doanh thu theo ngày trong tháng
    // =============================================================
    @GetMapping("/api/get-statistic-revenue-day-in-month")
    public List<DayInMonthStatistic> getDayInMonthStatistic(
            @RequestParam String month,
            @RequestParam String year,
            @RequestParam(required = false) Long branchId) {
        return statisticService.getDayInMonthStatistic(month, year, branchId);
    }

    // =============================================================
    // 🟩 2. Doanh thu theo ngày trong khoảng thời gian (fromDate - toDate)
    // =============================================================
    @GetMapping("/api/get-statistic-revenue-day-from-time")
    public List<DayInMonthStatistic2> getDayInMonthStatistic2(
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam(required = false) Long branchId) {
        return statisticService.getDailyRevenue2(fromDate, toDate, branchId);
    }

    // =============================================================
    // 🟩 3. Doanh thu theo tháng trong khoảng thời gian (MM-yyyy)
    // =============================================================
    @GetMapping("/api/get-statistic-revenue-month-from-time")
    public List<MonthInYearStatistic2> getMonthlyStatistic(
            @RequestParam String fromMonth,
            @RequestParam String toMonth,
            @RequestParam(required = false) Long branchId) {
        return statisticService.getMonthlyRevenue(fromMonth, toMonth, branchId);
    }

    // =============================================================
    // 🟩 4. Doanh thu theo tháng trong năm (ví dụ cho biểu đồ cột 12 tháng)
    // =============================================================
    @GetMapping("/api/get-statistic-revenue-month-in-year")
    public List<MonthInYearStatistic> getMonthInYearStatistic(
            @RequestParam String year,
            @RequestParam(required = false) Long branchId) {
        return statisticService.getMonthInYearStatistic(year, branchId);
    }

    // =============================================================
    // 🟦 5. Sản phẩm bán chạy theo thời gian
    // =============================================================
    @GetMapping("/api/get-bestseller-product")
    public List<BestSellerProduct> getBestSellerProductInTime(
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        return statisticService.getBestSellerProduct(fromDate, toDate);
    }

    // =============================================================
    // 🟦 6. Sản phẩm bán chạy toàn hệ thống
    // =============================================================
    @GetMapping("/api/get-bestseller-product-all")
    public List<BestSellerProduct> getBestSellerProductAll() {
        return statisticService.getBestSellerProductAll();
    }

    // =============================================================
    // 🟨 7. Thống kê sản phẩm trong thời gian
    // =============================================================
    @GetMapping("/api/get-statistic-product-time")
    public List<ProductStatistic> getStatisticProductInTime(
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        return statisticService.getStatisticProductInTime(fromDate, toDate);
    }

    // =============================================================
    // 🟨 8. Thống kê đơn hàng theo trạng thái
    // =============================================================
    @GetMapping("/api/get-statistic-order")
    public List<OrderStatistic> getStatisticOrder() {
        return statisticService.getStatisticOrder();
    }

    // =============================================================
    // 🧑‍💻 9. Thống kê người dùng (demo)
    // =============================================================
    @GetMapping("/get-statistic-user-by-month")
    public List<UserStatistic> getStatisticUserByMonth() {
        return accountService.getUserStatistics("2023-01-01", "2023-12-31");
    }
}
