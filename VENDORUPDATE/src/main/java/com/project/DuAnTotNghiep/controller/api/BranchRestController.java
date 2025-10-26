package com.project.DuAnTotNghiep.controller.api;

import com.project.DuAnTotNghiep.entity.Branch;
import com.project.DuAnTotNghiep.entity.Bill;
import com.project.DuAnTotNghiep.service.BranchService;
import com.project.DuAnTotNghiep.repository.BranchRepository;
import com.project.DuAnTotNghiep.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/branches")
@CrossOrigin(origins = "*")
public class BranchRestController {

    private static final Logger logger = LoggerFactory.getLogger(BranchRestController.class);

    @Autowired
    private BranchService branchService;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private BillRepository billRepository;

    /**
     * API chi tiết chi nhánh - doanh thu 7 ngày + 10 hóa đơn gần nhất
     * GET /api/branches/admin/{branchId}/detail
     */
    @GetMapping("/admin/{branchId}/detail")
    public ResponseEntity<?> getBranchDetail(@PathVariable Long branchId) {
        try {
            logger.info("Getting branch detail for branchId: {}", branchId);
            
            Optional<Branch> branchOpt = branchRepository.findById(branchId);
            if (!branchOpt.isPresent()) {
                logger.warn("Branch not found with id: {}", branchId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Chi nhánh không tồn tại"));
            }

            Branch branch = branchOpt.get();
            Map<String, Object> response = new HashMap<>();

            // 1. Lấy doanh thu 7 ngày gần nhất
            List<Object[]> dailyRevenueData = branchRepository.getDailyRevenueByBranch(branchId);
            List<String> revenueDates = new ArrayList<>();
            List<Double> revenueValues = new ArrayList<>();

            // Tạo dữ liệu đầy đủ 7 ngày (từ -6 ngày đến hôm nay)
            LocalDate today = LocalDate.now();
            Map<LocalDate, Double> revenueMap = new HashMap<>();
            
            // Khởi tạo tất cả 7 ngày với giá trị 0
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                revenueMap.put(date, 0.0);
            }
            
            // Cập nhật dữ liệu từ database nếu có
            if (dailyRevenueData != null && !dailyRevenueData.isEmpty()) {
                for (Object[] row : dailyRevenueData) {
                    if (row[0] != null && row[1] != null) {
                        try {
                            LocalDate date = LocalDate.parse(row[0].toString());
                            double revenue = ((Number) row[1]).doubleValue();
                            revenueMap.put(date, revenue);
                        } catch (Exception ex) {
                            logger.error("Error parsing revenue data", ex);
                        }
                    }
                }
            }
            
            // Sắp xếp theo ngày và thêm vào list
            revenueMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        revenueDates.add(entry.getKey().toString());
                        revenueValues.add(entry.getValue());
                    });

            response.put("branchId", branchId);
            response.put("branchName", branch.getBranchName());
            response.put("branchCode", branch.getBranchCode());
            response.put("revenueDates", revenueDates);
            response.put("revenueValues", revenueValues);

            logger.info("Revenue dates: {}, Revenue values: {}", revenueDates, revenueValues);

            // 2. Lấy 10 hóa đơn gần nhất của chi nhánh
            Pageable pageable = PageRequest.of(0, 20, Sort.by("createDate").descending());
            List<Bill> allBills = billRepository.findAll(pageable).getContent();
            
            List<Map<String, Object>> billList = new ArrayList<>();
            for (Bill bill : allBills) {
                try {
                    if (bill.getBranch() != null && bill.getBranch().getId() != null && bill.getBranch().getId().equals(branchId)) {
                        Map<String, Object> billMap = new HashMap<>();
                        billMap.put("id", bill.getId());
                        billMap.put("code", bill.getCode() != null ? bill.getCode() : "N/A");
                        billMap.put("createDate", bill.getCreateDate() != null ? bill.getCreateDate().toLocalDate().toString() : "");
                        billMap.put("totalAmount", bill.getAmount() != null ? bill.getAmount() : 0.0);
                        billMap.put("status", bill.getStatus() != null ? bill.getStatus().toString() : "N/A");
                        billList.add(billMap);
                        
                        if (billList.size() >= 10) break;
                    }
                } catch (Exception ex) {
                    logger.error("Error processing bill: {}", bill.getId(), ex);
                }
            }

            logger.info("Found {} bills for branch {}", billList.size(), branchId);
            response.put("bills", billList);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Lỗi lấy chi tiết chi nhánh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createBranch(@RequestBody Branch branch) {
        try {
            Branch createdBranch = branchService.createBranch(branch);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBranch);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating branch: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBranchById(@PathVariable Long id) {
        Optional<Branch> branch = branchService.getBranchById(id);
        if (branch.isPresent()) {
            return ResponseEntity.ok(branch.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Branch not found");
    }

    @GetMapping("/code/{branchCode}")
    public ResponseEntity<?> getBranchByCode(@PathVariable String branchCode) {
        Optional<Branch> branch = branchService.getBranchByCode(branchCode);
        if (branch.isPresent()) {
            return ResponseEntity.ok(branch.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Branch not found");
    }

    @GetMapping
    public ResponseEntity<?> getAllBranches() {
        List<Branch> branches = branchService.getAllBranches();
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveBranches() {
        List<Branch> branches = branchService.getActiveBranches();
        return ResponseEntity.ok(branches);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBranch(@PathVariable Long id, @RequestBody Branch branch) {
        try {
            Branch updatedBranch = branchService.updateBranch(id, branch);
            if (updatedBranch != null) {
                return ResponseEntity.ok(updatedBranch);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Branch not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating branch: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBranch(@PathVariable Long id) {
        try {
            branchService.deleteBranch(id);
            return ResponseEntity.ok("Branch deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting branch: " + e.getMessage());
        }
    }
}