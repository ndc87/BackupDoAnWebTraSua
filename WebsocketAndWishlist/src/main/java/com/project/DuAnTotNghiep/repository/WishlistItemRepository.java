package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.WishlistItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItemEntity, Long> {

    List<WishlistItemEntity> findByWishlistId(Long wishlistId);
}
