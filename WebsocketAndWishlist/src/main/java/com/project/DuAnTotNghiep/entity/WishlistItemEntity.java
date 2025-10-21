package com.project.DuAnTotNghiep.entity;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Bảng wishlist_item - chi tiết từng sản phẩm trong wishlist.
 */
@Entity
@Table(name = "wishlist_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_detail_id")
    private Long productDetailId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "wishlist_id")
    private Long wishlistId;

    /**
     * Quan hệ N-1: Mỗi wishlist_item thuộc về 1 wishlist.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", insertable = false, updatable = false)
    private WishlistEntity wishlist;
}
