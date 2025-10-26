package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByBranchCode(String branchCode);
    Optional<Branch> findByBranchName(String branchName);

    /**
     * Lấy tất cả chi nhánh đang hoạt động
     */
    List<Branch> findByIsActiveTrue();

    /**
     * Tìm chi nhánh theo email
     */
    Optional<Branch> findByEmail(String email);

    /**
     * Lấy doanh thu theo chi nhánh (tính doanh thu từ các hóa đơn 30 ngày gần nhất)
     */
    @Query(value = """
            SELECT 
                b.id,
                b.branch_code,
                b.branch_name,
                COALESCE(SUM(bill.amount), 0) as totalRevenue
            FROM branch b
            LEFT JOIN bill bill ON b.id = bill.branch_id
            WHERE bill.create_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)
            GROUP BY b.id, b.branch_code, b.branch_name
            ORDER BY totalRevenue DESC
            """, nativeQuery = true)
    List<Object[]> getBranchRevenueStatistics();

    /**
     * Lấy doanh thu theo ngày cho 1 chi nhánh (7 ngày gần nhất)
     */
    @Query(value = """
            SELECT 
                DATE(bill.create_date) as revenue_date,
                COALESCE(SUM(bill.amount), 0) as total_revenue
            FROM bill
            WHERE bill.branch_id = :branchId 
                AND bill.create_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
                AND bill.create_date < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
            GROUP BY DATE(bill.create_date)
            ORDER BY revenue_date ASC
            """, nativeQuery = true)
    List<Object[]> getDailyRevenueByBranch(@Param("branchId") Long branchId);
}