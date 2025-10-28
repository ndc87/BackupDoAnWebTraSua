package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByEmail(String email);

    /**
     * ✅ Thống kê số lượng tài khoản được tạo theo tháng
     * @param startDate - ngày bắt đầu (ví dụ: "2025-01-01")
     * @param endDate - ngày kết thúc (ví dụ: "2025-12-31")
     * @return List<Object[]> [month, count]
     */
    @Query(value = """
            SELECT 
                FORMAT(a.create_date, 'MM-yyyy') AS month,
                COUNT(a.id) AS count
            FROM account a
            WHERE a.create_date BETWEEN :startDate AND :endDate
            GROUP BY FORMAT(a.create_date, 'MM-yyyy')
            ORDER BY MIN(a.create_date)
            """, nativeQuery = true)
    List<Object[]> getMonthlyAccountStatistics(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    Account findByCustomer_PhoneNumber(String phoneNumber);

    Account findTopByOrderByIdDesc();
}
