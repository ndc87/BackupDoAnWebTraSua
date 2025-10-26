package com.project.DuAnTotNghiep.service;

import com.project.DuAnTotNghiep.entity.Branch;
import java.util.List;
import java.util.Optional;

public interface BranchService {
    Branch createBranch(Branch branch);
    Optional<Branch> getBranchById(Long id);
    Optional<Branch> getBranchByCode(String branchCode);
    List<Branch> getAllBranches();
    Branch updateBranch(Long id, Branch branch);
    void deleteBranch(Long id);
    List<Branch> getActiveBranches();
}
