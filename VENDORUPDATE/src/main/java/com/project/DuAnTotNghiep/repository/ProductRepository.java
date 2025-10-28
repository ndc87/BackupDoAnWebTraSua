package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.dto.Product.ProductSearchDto;
import com.project.DuAnTotNghiep.dto.Statistic.BestSellerProduct;
import com.project.DuAnTotNghiep.dto.Statistic.ProductStatistic;
import com.project.DuAnTotNghiep.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Product findByCode(String code);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
    Page<Product> getAllByName(String name, Pageable pageable);

    boolean existsByCode(String code);

    Page<Product> findAllByStatus(int status, Pageable pageable);
    Page<Product> findAllByStatusAndDeleteFlag(int status, boolean deleteFlag, Pageable pageable);
    
    @Query(value = "SELECT p.id, p.code, p.name, br.name as brandName, mt.name as materialName, ct.name as categoryName " +
            "FROM product p " +
            "INNER JOIN brand br ON br.id = p.brand_id " +
            "INNER JOIN material mt ON mt.id = p.material_id " +
            "INNER JOIN category ct ON ct.id = p.category_id " +
            "WHERE p.name LIKE CONCAT('%', :productName, '%') AND p.delete_flag = 0",
            nativeQuery = true)
    Page<Product> searchProductName(String productName, Pageable pageable);

    @Query(value = "SELECT p.id AS idSanPham, p.code AS maSanPham, p.name AS tenSanPham, " +
            "b.name AS nhanHang, m.name AS chatLieu, c.name AS theLoai, p.status AS trangThai " +
            "FROM product p " +
            "LEFT JOIN brand b ON b.id = p.brand_id " +
            "LEFT JOIN material m ON m.id = p.material_id " +
            "LEFT JOIN category c ON c.id = p.category_id " +
            "WHERE (:maSanPham IS NULL OR p.code LIKE CONCAT('%', :maSanPham, '%')) AND " +
            "(:tenSanPham IS NULL OR p.name LIKE CONCAT('%', :tenSanPham, '%')) AND " +
            "(:nhanHang IS NULL OR b.id = :nhanHang) AND " +
            "(:chatLieu IS NULL OR m.id = :chatLieu) AND " +
            "(:theLoai IS NULL OR c.id = :theLoai) AND " +
            "(:trangThai IS NULL OR p.status = :trangThai) AND p.delete_flag = 0",
            nativeQuery = true)
    Page<ProductSearchDto> listSearchProduct(String maSanPham, String tenSanPham, Long nhanHang,
                                             Long chatLieu, Long theLoai, Integer trangThai, Pageable pageable);

    @Query(value = "SELECT p.id AS idSanPham, p.code AS maSanPham, p.name AS tenSanPham, " +
            "b.name AS nhanHang, m.name AS chatLieu, c.name AS theLoai, p.status AS trangThai " +
            "FROM product p " +
            "LEFT JOIN brand b ON b.id = p.brand_id " +
            "LEFT JOIN material m ON m.id = p.material_id " +
            "LEFT JOIN category c ON c.id = p.category_id " +
            "WHERE p.delete_flag = 0",
            nativeQuery = true)
    Page<ProductSearchDto> getAll(Pageable pageable);

    Page<Product> findAllByDeleteFlagFalse(Pageable pageable);

    @Query("select p from Product p join ProductDetail pd on p.id = pd.product.id where pd.id = :productDetaiLId")
    Product findByProductDetail_Id(Long productDetaiLId);

    Product findTopByOrderByIdDesc();

    @Query(value = "SELECT top(10) p.id, p.code, p.name, (SELECT top(1) image.link FROM image WHERE image.product_id = p.id) as imageUrl, brand.name as brand, cat.name as category, COALESCE(SUM(bd.quantity),0) AS totalQuantity, COALESCE(SUM(bd.return_quantity),0) AS totalQuantityReturn, SUM(bd.quantity * bd.moment_price) as revenue\n" +
            "             from bill b join bill_detail bd on b.id = bd.bill_id join product_detail pd on pd.id = bd.product_detail_id \n" +
            "            join product p on p.id = pd.product_id left join bill_return br on br.bill_id = b.id\n" +
            "            join brand on brand.id = p.brand_id join category cat on cat.id = p.category_id\n" +
            "            where b.status = 'HOAN_THANH'\n" +
            "            group by p.id, p.code, p.name, brand.name, cat.name order by totalQuantity DESC", nativeQuery = true)
    List<BestSellerProduct> getBestSellerProduct();

    @Query(value = "SELECT top(10)  p.id, p.code, p.name, (SELECT top(1) image.link FROM image WHERE image.product_id = p.id) as imageUrl , brand.name as brand, cat.name as category, COALESCE(SUM(bd.quantity),0) AS totalQuantity, COALESCE(SUM(bd.return_quantity),0) AS totalQuantityReturn, SUM(bd.quantity * bd.moment_price) as revenue\n" +
            "             from bill b join bill_detail bd on b.id = bd.bill_id join product_detail pd on pd.id = bd.product_detail_id \n" +
            "            join product p on p.id = pd.product_id left join bill_return br on br.bill_id = b.id\n" +
            "            join brand on brand.id = p.brand_id join category cat on cat.id = p.category_id\n" +
            "            where b.status = 'HOAN_THANH' and b.create_date BETWEEN :fromDate AND :toDate \n" +
            "            group by p.id, p.code, p.name, brand.name, cat.name order by totalQuantity DESC", nativeQuery = true)
    List<BestSellerProduct> getBestSellerProduct(String fromDate, String toDate);

//    @Query(value = "SELECT  p.id, p.code, p.name, COALESCE(SUM(bd.quantity),0) AS totalQuantity, COALESCE(SUM(bd.return_quantity),0) AS totalQuantityReturn, SUM(bd.quantity * bd.moment_price) as revenue\n" +
//            "FROM bill b\n" +
//            "\t JOIN bill_detail bd ON bd.id = b.id\n" +
//            "\t JOIN product_detail pd ON bd.id = pd.id\n" +
//            "\t JOIN product p on p.id = pd.product_id\n" +
//            "\tleft join bill_return br ON b.id = br.bill_id\n" +
//            "\tleft JOIN return_detail rd ON br.id = rd.id\n" +
//            "\t JOIN image i on i.product_id = p.id\n" +
//            "WHERE b.status = 'HOAN_THANH' AND b.create_date BETWEEN :fromDate AND :toDate\n" +
//            "GROUP BY p.id, p.code, p.name",nativeQuery = true)
//    List<ProductStatistic> getStatisticProduct(String fromDate, String toDate);

    @Query(value = "SELECT\n" +
            "    p.id,\n" +
            "    p.code,\n" +
            "    p.name,\n" +
            "    brand.name AS brand,\n" +
            "    cat.name AS category,\n" +
            "    COALESCE(SUM(bd.quantity), 0) AS totalQuantity,\n" +
            "    COALESCE(SUM(bd.return_quantity), 0) AS totalQuantityReturn,\n" +
            "    COALESCE(SUM(bd.quantity * bd.moment_price), 0) AS revenue\n" +
            "FROM\n" +
            "    product p\n" +
            "JOIN\n" +
            "    product_detail pd ON p.id = pd.product_id\n" +
            "LEFT JOIN\n" +
            "    bill_detail bd ON pd.id = bd.product_detail_id\n" +
            "LEFT JOIN\n" +
            "    bill b ON bd.bill_id = b.id\n" +
            "LEFT JOIN\n" +
            "    brand ON brand.id = p.brand_id \n" +
            "LEFT JOIN\n" +
            "    category cat ON cat.id = p.category_id\n" +
            "WHERE (b.status = 'HOAN_THANH' AND b.create_date between :fromDate AND :toDate OR b.status IS NULL) \n" +
            "GROUP BY\n" +
            "    p.id, p.code, p.name, brand.name, cat.name;", nativeQuery = true)
    List<ProductStatistic> getStatisticProduct(String fromDate, String toDate);
}