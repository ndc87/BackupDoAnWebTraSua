package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Color;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColorRepository extends JpaRepository<Color, Long> {
	boolean existsByCode(String code);
    List<Color> findAllByDeleteFlagFalse();

    Page<Color> findAllByDeleteFlagFalse(Pageable pageable);
}