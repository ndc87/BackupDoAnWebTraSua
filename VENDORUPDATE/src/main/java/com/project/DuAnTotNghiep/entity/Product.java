package com.project.DuAnTotNghiep.entity;

import lombok.*;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @Nationalized
    private String name;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    private int status;

    @Column(name = "delete_flag", nullable = false)
    private boolean deleteFlag;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int gender;

    @Nationalized
    @Column(name = "describe")
    private String describe;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;
    
    @ManyToOne
    @JoinColumn(name = "color_id")
    private Color color; // them vo db

    // 🔗 Quan hệ 1-nhiều với bảng image
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> image = new ArrayList<>();

    // 🔗 Quan hệ 1-nhiều với bảng product_detail
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductDetail> productDetails = new ArrayList<>();

    // ✅ Hàm tiện ích: lấy ảnh đầu tiên
    public String getFirstImageUrl() {
        if (image != null && !image.isEmpty()) {
            return image.get(0).getLink();
        }
        return "/images/default-product.png";
    }

    // ✅ Hàm tiện ích: lấy giá thấp nhất trong các chi tiết
    public double getMinPrice() {
        if (productDetails == null || productDetails.isEmpty()) {
            return price;
        }
        return productDetails.stream()
                .mapToDouble(ProductDetail::getPrice)
                .min()
                .orElse(price);
    }
    
    public Long getId() {
        return this.id;
    }
}
