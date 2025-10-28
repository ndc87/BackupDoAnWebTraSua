package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByCode(String code);
    List<Brand> findAllByDeleteFlagFalse();
    Page<Brand> findAllByDeleteFlagFalse(Pageable pageable);
}