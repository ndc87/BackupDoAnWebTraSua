package com.project.DuAnTotNghiep.service;

import com.project.DuAnTotNghiep.entity.BranchInventory;
import com.project.DuAnTotNghiep.entity.Branch;
import com.project.DuAnTotNghiep.entity.ProductDetail;
import java.util.List;
import java.util.Optional;

public interface BranchInventoryService {
    BranchInventory createInventory(BranchInventory inventory);
    Optional<BranchInventory> getInventoryById(Long id);
    Optional<BranchInventory> getInventoryByBranchAndProduct(Long branchId, Long productDetailId);
    List<BranchInventory> getInventoriesByBranchId(Long branchId);
    List<BranchInventory> getInventoriesByProductDetailId(Long productDetailId);
    List<BranchInventory> getAllInventories();
    BranchInventory updateInventory(Long id, BranchInventory inventory);
    void deleteInventory(Long id);
    void updateQuantity(Long branchId, Long productDetailId, int quantity);
}
