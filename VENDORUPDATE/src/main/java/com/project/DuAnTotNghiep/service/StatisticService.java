package com.project.DuAnTotNghiep.service;

import com.project.DuAnTotNghiep.dto.Statistic.*;
import java.util.List;

public interface StatisticService {

    // 🟩 Thống kê doanh thu theo ngày trong tháng
    List<DayInMonthStatistic> getDayInMonthStatistic(String month, String year, Long branchId);

    // 🟩 Thống kê doanh thu theo tháng trong năm
    List<MonthInYearStatistic> getMonthInYearStatistic(String year, Long branchId);

    // 🟩 Thống kê doanh thu giữa hai tháng (dạng MM-yyyy)
    List<MonthInYearStatistic2> getMonthlyRevenue(String fromDate, String toDate, Long branchId);

    // 🟩 Thống kê doanh thu theo ngày (fromDate → toDate)
    List<DayInMonthStatistic2> getDailyRevenue2(String startDate, String endDate, Long branchId);

    // 🟦 Sản phẩm bán chạy trong khoảng thời gian
    List<BestSellerProduct> getBestSellerProduct(String fromDate, String toDate);

    // 🟦 Sản phẩm bán chạy toàn hệ thống
    List<BestSellerProduct> getBestSellerProductAll();

    // 🟨 Thống kê sản phẩm trong khoảng thời gian
    List<ProductStatistic> getStatisticProductInTime(String fromDate, String toDate);

    // 🟨 Thống kê số đơn hàng theo trạng thái
    List<OrderStatistic> getStatisticOrder();
    
    List<BestSellerProduct> getBestSellerProductByBranch(Long branchId);

}
