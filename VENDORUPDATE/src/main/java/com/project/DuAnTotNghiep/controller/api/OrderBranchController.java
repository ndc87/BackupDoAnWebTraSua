package com.project.DuAnTotNghiep.controller.api;

import com.project.DuAnTotNghiep.entity.*;
import com.project.DuAnTotNghiep.entity.enumClass.BillStatus;
import com.project.DuAnTotNghiep.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class OrderBranchController {

    private static final Logger logger = LoggerFactory.getLogger(OrderBranchController.class);

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BillDetailRepository billDetailRepository;

    @Autowired
    private ProductDetailRepository productDetailRepository;

    @Autowired
    private BranchInventoryRepository branchInventoryRepository;

    /**
     * ✅ Lấy danh sách chi nhánh đang hoạt động
     */
    @GetMapping("/branches")
    public ResponseEntity<?> getActiveBranches() {
        try {
            logger.info("🔍 Getting active branches from database");

            List<Branch> branches = branchRepository.findByIsActiveTrue();
            
            if (branches == null) {
                logger.warn("⚠️ Branches list is null from database");
                branches = new ArrayList<>();
            }
            
            logger.info("✅ Found {} active branches", branches.size());

            List<Map<String, Object>> branchList = new ArrayList<>();
            for (Branch branch : branches) {
                try {
                    if (branch == null) {
                        logger.warn("⚠️ Branch object is null, skipping");
                        continue;
                    }
                    
                    Map<String, Object> b = new HashMap<>();
                    b.put("id", branch.getId() != null ? branch.getId() : 0L);
                    b.put("branchCode", branch.getBranchCode() != null ? branch.getBranchCode() : "");
                    b.put("branchName", branch.getBranchName() != null ? branch.getBranchName() : "");
                    b.put("name", branch.getBranchName() != null ? branch.getBranchName() : "");
                    b.put("address", branch.getAddress() != null ? branch.getAddress() : "");
                    b.put("phone", branch.getPhone() != null ? branch.getPhone() : "");
                    b.put("email", branch.getEmail() != null ? branch.getEmail() : "");
                    b.put("isActive", branch.isActive());
                    branchList.add(b);
                    logger.debug("✅ Mapped branch: {}", branch.getBranchName());
                } catch (Exception e) {
                    logger.error("❌ Lỗi khi map dữ liệu chi nhánh: {}", e.getMessage(), e);
                }
            }

            logger.info("✅ Successfully processed {} branches", branchList.size());
            return ResponseEntity.ok(branchList);
            
        } catch (NullPointerException e) {
            logger.error("❌ NullPointerException - Database connection error or null branch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi kết nối database hoặc dữ liệu null"));
        } catch (Exception e) {
            logger.error("❌ Error getting active branches", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi: " + e.getMessage(), "type", e.getClass().getSimpleName()));
        }
    }
    
    /**
     * ✅ Lấy danh sách sản phẩm còn hàng của một chi nhánh
     */
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<?> getProductsByBranch(@PathVariable Long branchId) {
        try {
            // 🔹 Kiểm tra chi nhánh tồn tại
            if (!branchRepository.existsById(branchId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Chi nhánh không tồn tại"));
            }

            // 🔹 Lấy danh sách tồn kho còn hàng
            List<BranchInventory> inventories = branchInventoryRepository.findActiveProductsByBranch(branchId);

            // 🔹 Chuyển sang dạng JSON trả về frontend
            List<Map<String, Object>> products = new ArrayList<>();
            for (BranchInventory inv : inventories) {
                ProductDetail pd = inv.getProductDetail();
                if (pd == null || pd.getProduct() == null) continue;

                Map<String, Object> p = new HashMap<>();
                p.put("productDetailId", pd.getId());
                p.put("productName", pd.getProduct().getName());
                p.put("price", pd.getProduct().getPrice());
                p.put("quantity", inv.getQuantity());
                p.put("color", pd.getColor() != null ? pd.getColor().getName() : null);
                p.put("size", pd.getSize() != null ? pd.getSize().getName() : null);
                p.put("category", pd.getProduct().getCategory() != null ? pd.getProduct().getCategory().getName() : null);
                products.add(p);
            }

            // 🔹 Gói vào object thống nhất
            Map<String, Object> response = Map.of(
                "branchId", branchId,
                "productCount", products.size(),
                "products", products
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi tải sản phẩm: " + e.getMessage()));
        }
    }


    /**
     * ✅ Ghi nhận chi nhánh mà user chọn
     */
    @PostMapping("/select-branch")
    public ResponseEntity<?> selectBranch(@RequestBody Map<String, Object> request) {
        try {
            Object idObj = request.get("branchId");
            if (idObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Chi nhánh không hợp lệ"));
            }

            Long branchId = Long.parseLong(idObj.toString());
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new Exception("Chi nhánh không tồn tại"));

            if (!branch.isActive()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Chi nhánh không hoạt động"));
            }

            logger.info("Branch selected: {} - {}", branchId, branch.getBranchName());

            return ResponseEntity.ok(Map.of(
                    "message", "Chọn chi nhánh thành công",
                    "branchId", branch.getId(),
                    "branchName", branch.getBranchName(),
                    "branchCode", branch.getBranchCode(),
                    "address", branch.getAddress(),
                    "phone", branch.getPhone()
            ));

        } catch (Exception e) {
            logger.error("Error selecting branch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

    /**
     * ✅ Tạo đơn hàng gắn với chi nhánh
     */
    @PostMapping("/create-with-branch")
    public ResponseEntity<?> createOrderWithBranch(@RequestBody Map<String, Object> request) {
        try {
            logger.info("Creating order with branch...");

            Long branchId = Long.parseLong(request.get("branchId").toString());
            Long customerId = Long.parseLong(request.get("customerId").toString());
            String billingAddress = (String) request.get("billingAddress");
            List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");

            if (items == null || items.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống"));
            }

            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new Exception("Chi nhánh không tồn tại"));

            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new Exception("Khách hàng không tồn tại"));

            Bill bill = new Bill();
            bill.setCode("ORD-" + System.currentTimeMillis());
            bill.setCustomer(customer);
            bill.setBillingAddress(billingAddress);
            bill.setBranch(branch);
            bill.setStatus(BillStatus.CHO_XAC_NHAN);
            bill.setCreateDate(LocalDateTime.now());
            bill.setAmount(0.0);

            double totalAmount = 0.0;
            List<BillDetail> billDetails = new ArrayList<>();

            for (Map<String, Object> item : items) {
                Long productDetailId = Long.parseLong(item.get("productDetailId").toString());
                Integer quantity = Integer.parseInt(item.get("quantity").toString());

                ProductDetail productDetail = productDetailRepository.findById(productDetailId)
                        .orElseThrow(() -> new Exception("Sản phẩm không tồn tại"));

                Integer availableQuantity = branchInventoryRepository
                        .getQuantityByBranchAndProductDetail(branchId, productDetailId);

                if (availableQuantity == null || availableQuantity < quantity) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Sản phẩm " +
                                    productDetail.getProduct().getName() +
                                    " không đủ hàng tại chi nhánh. Còn lại: " +
                                    (availableQuantity != null ? availableQuantity : 0)));
                }

                BillDetail billDetail = new BillDetail();
                billDetail.setBill(bill);
                billDetail.setProductDetail(productDetail);
                billDetail.setQuantity(quantity);
                billDetail.setMomentPrice(productDetail.getProduct().getPrice());
                billDetail.setReturnQuantity(0);

                double itemAmount = productDetail.getProduct().getPrice() * quantity;
                totalAmount += itemAmount;
            }

            bill.setAmount(totalAmount);
            Bill savedBill = billRepository.save(bill);

            logger.info("✅ Order created: {} at branch {}", savedBill.getId(), branch.getBranchName());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Đặt hàng thành công!",
                    "billId", savedBill.getId(),
                    "billCode", savedBill.getCode(),
                    "branchId", branchId,
                    "totalAmount", totalAmount,
                    "itemCount", items.size()
            ));

        } catch (Exception e) {
            logger.error("Error creating order with branch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

    /**
     * ✅ Lấy danh sách đơn hàng của 1 khách hàng tại 1 chi nhánh
     */
    @GetMapping("/customer/{customerId}/branch/{branchId}")
    public ResponseEntity<?> getOrdersByCustomerAndBranch(
            @PathVariable Long customerId,
            @PathVariable Long branchId) {
        try {
            List<Bill> bills = billRepository.findAll().stream()
                    .filter(b -> b.getCustomer() != null &&
                            b.getCustomer().getId().equals(customerId) &&
                            b.getBranch() != null &&
                            b.getBranch().getId().equals(branchId))
                    .toList();

            List<Map<String, Object>> orders = new ArrayList<>();
            for (Bill b : bills) {
                Map<String, Object> o = new HashMap<>();
                o.put("billId", b.getId());
                o.put("billCode", b.getCode());
                o.put("amount", b.getAmount());
                o.put("status", b.getStatus());
                o.put("createDate", b.getCreateDate());
                o.put("itemCount", b.getBillDetail() != null ? b.getBillDetail().size() : 0);
                orders.add(o);
            }

            return ResponseEntity.ok(Map.of(
                    "customerId", customerId,
                    "branchId", branchId,
                    "totalOrders", orders.size(),
                    "orders", orders
            ));

        } catch (Exception e) {
            logger.error("Error getting orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

    /**
     * ✅ Lấy chi tiết 1 đơn hàng cụ thể
     */
    @GetMapping("/{billId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long billId) {
        try {
            Bill bill = billRepository.findById(billId)
                    .orElseThrow(() -> new Exception("Đơn hàng không tồn tại"));

            Map<String, Object> order = new HashMap<>();
            order.put("billId", bill.getId());
            order.put("billCode", bill.getCode());
            order.put("amount", bill.getAmount());
            order.put("status", bill.getStatus());
            order.put("createDate", bill.getCreateDate());
            order.put("billingAddress", bill.getBillingAddress());

            if (bill.getBranch() != null) {
                Map<String, Object> branch = new HashMap<>();
                branch.put("id", bill.getBranch().getId());
                branch.put("name", bill.getBranch().getBranchName());
                branch.put("address", bill.getBranch().getAddress());
                order.put("branch", branch);
            }

            List<Map<String, Object>> items = new ArrayList<>();
            if (bill.getBillDetail() != null) {
                for (BillDetail d : bill.getBillDetail()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("productName", d.getProductDetail().getProduct().getName());
                    item.put("quantity", d.getQuantity());
                    item.put("price", d.getMomentPrice());
                    item.put("amount", d.getMomentPrice() * d.getQuantity());
                    items.add(item);
                }
            }

            order.put("items", items);
            return ResponseEntity.ok(order);

        } catch (Exception e) {
            logger.error("Error getting order detail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }
}
