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
@Table(name = "BranchInventory", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"branch_id", "product_detail_id"})
})
public class BranchInventory implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id")
    private ProductDetail productDetail;

    private Integer quantity = 0;

    private Integer minQuantity = 0;

    private Integer maxQuantity;

    private boolean isActive = true;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;
}
