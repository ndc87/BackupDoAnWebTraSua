package com.project.DuAnTotNghiep.entity;

import lombok.*;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "branch_inventory", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"branch_id", "product_detail_id"})
})
public class BranchInventory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id", nullable = false)
    private ProductDetail productDetail;

    @Column(name = "quantity")
    private Integer quantity = 0;

    @Column(name = "minQuantity")
    private Integer minQuantity = 0;

    @Column(name = "maxQuantity")
    private Integer maxQuantity;

    @Column(name = "isActive") // 👈 đúng với cột trong bảng
    private boolean isActive = true;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;
}