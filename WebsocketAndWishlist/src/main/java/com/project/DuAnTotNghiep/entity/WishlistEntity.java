package com.project.DuAnTotNghiep.entity;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bảng wishlist - lưu danh sách yêu thích của mỗi tài khoản.
 */
@Entity
@Table(name = "wishlist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "user_id")
    private Long userId;

    /**
     * Quan hệ 1-N: Một wishlist có nhiều wishlist_item
     */
    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WishlistItemEntity> items;
}
