package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.entity.WishlistEntity;
import com.project.DuAnTotNghiep.entity.WishlistItemEntity;
import com.project.DuAnTotNghiep.repository.WishlistRepository;
import com.project.DuAnTotNghiep.repository.WishlistItemRepository;
import com.project.DuAnTotNghiep.service.WishlistService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepo;
    private final WishlistItemRepository itemRepo;

    public WishlistServiceImpl(WishlistRepository wishlistRepo, WishlistItemRepository itemRepo) {
        this.wishlistRepo = wishlistRepo;
        this.itemRepo = itemRepo;
    }

    @Override
    public List<WishlistItemEntity> getWishlistItems(Long accountId) {
        return wishlistRepo.findByAccountId(accountId)
                .map(w -> itemRepo.findByWishlistId(w.getId()))
                .orElse(List.of());
    }

    @Override
    public int countWishlistItems(Long accountId) {
        return wishlistRepo.findByAccountId(accountId)
                .map(w -> itemRepo.findByWishlistId(w.getId()).size())
                .orElse(0);
    }
}
