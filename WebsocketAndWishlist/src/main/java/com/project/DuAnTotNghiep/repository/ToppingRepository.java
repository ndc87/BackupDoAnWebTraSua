package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToppingRepository extends JpaRepository<Topping, Long> {

    // ✅ Truy vấn lấy các topping có status = 1
    @Query("SELECT t FROM Topping t WHERE t.status = 1 ORDER BY t.name ASC")
    List<Topping> findAllActiveToppings();

    boolean existsByName(String name);
}
