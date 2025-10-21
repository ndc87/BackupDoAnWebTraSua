package com.project.DuAnTotNghiep.service;

import com.project.DuAnTotNghiep.entity.WishlistItemEntity;
import java.util.List;

public interface WishlistService {
    List<WishlistItemEntity> getWishlistItems(Long accountId);
    int countWishlistItems(Long accountId);
}
