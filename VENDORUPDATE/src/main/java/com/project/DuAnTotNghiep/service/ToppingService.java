// Đường dẫn: WebsocketAndWishlist/src/main/java/com/project/DuAnTotNghiep/service/ToppingService.java

package com.project.DuAnTotNghiep.service;

import com.project.DuAnTotNghiep.dto.Topping.ToppingDto;

import java.util.List;

public interface ToppingService {
    
    List<ToppingDto> getAllToppings();
    
    List<ToppingDto> getActiveToppings();
    
    ToppingDto getToppingById(Long id);
    
    ToppingDto createTopping(ToppingDto toppingDTO);
    
    ToppingDto updateTopping(Long id, ToppingDto toppingDTO);
    
    void deleteTopping(Long id);
    
    void toggleStatus(Long id);
}