package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.WishlistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistEntity, Long> {

    Optional<WishlistEntity> findByAccountId(Long accountId);
}
