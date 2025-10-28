package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Category;
import com.project.DuAnTotNghiep.entity.Size;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByCode(String code);
    List<Category> findAllByDeleteFlagFalse();
    Page<Category> findAllByDeleteFlagFalse(Pageable pageable);
    
}