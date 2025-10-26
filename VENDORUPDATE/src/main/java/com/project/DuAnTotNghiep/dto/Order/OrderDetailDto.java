package com.project.DuAnTotNghiep.dto.Order;

import com.project.DuAnTotNghiep.dto.Product.ProductDetailDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//import thêm:
import java.util.List;
import com.project.DuAnTotNghiep.dto.Order.ToppingOrderDto;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {
    private Long productDetailId;
    private Integer quantity;
    private List<ToppingOrderDto> toppings; // ✅ thêm dòng này
}
