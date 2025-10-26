package com.project.DuAnTotNghiep.service;

import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.entity.Branch;
import com.project.DuAnTotNghiep.dto.Account.CreateVendorAccountRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AccountBranchService {
    // Gán branch cho account hiện có
    Account assignBranchToAccount(Long accountId, Long branchId);
    
    // Tạo vendor account mới kèm branch
    Account createVendorAccountWithBranch(CreateVendorAccountRequest request);
    
    // Lấy account theo branch
    Optional<Account> getVendorAccountByBranchId(Long branchId);
    
    // Xóa branch khỏi account
    Account removeBranchFromAccount(Long accountId);
    
    // Gán branch có thông tin mới cho account hiện có
    Account assignBranchWithInfo(Long accountId, Branch branch);
    
    // Lấy danh sách account chưa có branch
    List<Account> getAccountsWithoutBranch();
    
    // Tìm account theo ID
    Optional<Account> findAccountById(Long accountId);

    // 📊 Thống kê doanh thu chi nhánh (thêm mới)
    List<Map<String, Object>> getBranchRevenueStatistics();
}
