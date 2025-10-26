package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.BranchInventory;
import com.project.DuAnTotNghiep.entity.Branch;
import com.project.DuAnTotNghiep.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchInventoryRepository extends JpaRepository<BranchInventory, Long> {
    Optional<BranchInventory> findByBranchIdAndProductDetailId(Long branchId, Long productDetailId);
    List<BranchInventory> findByBranchId(Long branchId);
    List<BranchInventory> findByProductDetailId(Long productDetailId);
    List<BranchInventory> findByBranch(Branch branch);
    List<BranchInventory> findByProductDetail(ProductDetail productDetail);
}
