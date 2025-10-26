# 🔧 Hướng Dẫn Khắc Phục Lỗi Tải Chi Tiết Chi Nhánh

## 📋 Các Bước Kiểm Tra

### 1️⃣ Kiểm Tra Browser Console (F12)
Mở Developer Tools và xem mục **Console** để tìm lỗi JavaScript:
- Nhấn **F12** hoặc **Ctrl+Shift+I** (Windows)
- Chọn tab **Console**
- Click nút "Xem chi tiết" và xem lỗi nào xuất hiện

**Lỗi có thể gặp:**
```
Loading branch detail for branchId: 1
Response status: 404
HTTP error! status: 404
```
→ Endpoint API không tồn tại

```
Loading branch detail for branchId: 1
Response status: 200
Branch detail data received: {error: "Chi nhánh không tồn tại"}
```
→ Chi nhánh không có dữ liệu

---

### 2️⃣ Kiểm Tra Network Tab (Ctrl+Shift+I → Network)
- Mở tab **Network**
- Click nút "Xem chi tiết" 
- Tìm request `/api/branches/admin/*/detail`
- Kiểm tra:
  - ✅ Status: 200 (OK)
  - ✅ Response: JSON hợp lệ

**Xem Response JSON:**
```json
{
  "branchId": 1,
  "branchName": "Chi nhánh Hà Nội",
  "branchCode": "BH001",
  "revenueDates": ["2025-10-20", "2025-10-21", ...],
  "revenueValues": [100000, 150000, ...],
  "bills": [{...}, {...}]
}
```

---

### 3️⃣ Kiểm Tra Application Server Logs
Xem logs của Spring Boot để tìm lỗi backend:

**Lỗi có thể gặp:**
```
ERROR com.project.DuAnTotNghiep.controller.api.BranchRestController 
- Lỗi lấy chi tiết chi nhánh
java.lang.NullPointerException: Cannot invoke method on null object
```
→ Dữ liệu Branch hoặc Bill là null

```
ERROR - Lỗi: Branch with id 999 not found
```
→ Branch ID không tồn tại

---

## 🔍 Các Nguyên Nhân Lỗi Thường Gặp

### ❌ Lỗi 1: API Endpoint Không Tồn Tại (404)
**Nguyên nhân:** Controller chưa được @RequestMapping đúng cách

**Giải pháp:**
```java
@RestController
@RequestMapping("/api/branches")
@CrossOrigin(origins = "*")  // ← Thêm này
public class BranchRestController {
    
    @GetMapping("/admin/{branchId}/detail")  // ← Đây là endpoint
    public ResponseEntity<?> getBranchDetail(@PathVariable Long branchId) {
        // ...
    }
}
```

**URL phải là:** `http://localhost:8080/api/branches/admin/1/detail`

---

### ❌ Lỗi 2: Bill.branch Là Null (không có liên kết)
**Nguyên nhân:** Dữ liệu Bill trong database chưa có branch_id

**Kiểm tra SQL:**
```sql
-- Kiểm tra xem Bill có branch_id không
SELECT b.id, b.code, b.branch_id FROM bill b LIMIT 10;

-- Nếu branch_id toàn null, cập nhật:
UPDATE bill SET branch_id = 1 WHERE branch_id IS NULL AND customer_id = 1;
```

**Giải pháp:** 
- Đảm bảo khi tạo Bill, phải set branch
- Cập nhật dữ liệu cũ trong database

---

### ❌ Lỗi 3: Query Doanh Thu Không Trả Về Dữ Liệu
**Nguyên nhân:** Query SQL sai hoặc không có dữ liệu

**Kiểm tra Query:**
```sql
-- Test query lấy doanh thu 7 ngày
SELECT 
    DATE(bill.create_date) as revenue_date,
    COALESCE(SUM(bill.amount), 0) as total_revenue
FROM bill
WHERE bill.branch_id = 1 
    AND bill.create_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
    AND bill.create_date < DATE_ADD(CURDATE(), INTERVAL 1 DAY)
GROUP BY DATE(bill.create_date)
ORDER BY revenue_date ASC;
```

**Giải pháp:**
- Nếu không có dữ liệu → API tự tạo 7 ngày với giá trị 0.0
- Kiểm tra timezone của database và server

---

### ❌ Lỗi 4: Chart.js Không Hiển Thị
**Nguyên nhân:** 
- Canvas element không tồn tại
- Dữ liệu labels/data rỗng
- Chart.js chưa được load

**Giải pháp:**
```html
<!-- Đảm bảo có trong file -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<!-- Canvas phải có id đúng -->
<canvas id="branchDetailRevenueChart" style="max-height: 300px;"></canvas>

<!-- Debug trong console -->
<script>
function drawRevenueChart(dates, values) {
    console.log('Drawing chart with dates:', dates, 'values:', values);
    if (!dates || dates.length === 0) {
        console.error('No dates provided!');
        return;
    }
    // ...
}
</script>
```

---

## ✅ Danh Sách Kiểm Tra Trước Khi Test

- [ ] `BranchRestController.java` đã compile thành công
- [ ] `BranchRepository.java` có phương thức `getDailyRevenueByBranch()`
- [ ] `Bill.java` có trường `branch` và quan hệ `@ManyToOne`
- [ ] Spring Boot server đang chạy (port 8080)
- [ ] Database có dữ liệu Branch và Bill
- [ ] Bill có liên kết với Branch (branch_id != null)
- [ ] File HTML `branch-management.html` đã được lưu
- [ ] Browser cache đã được clear (Ctrl+Shift+Delete)

---

## 🚀 Test Step-by-Step

### Step 1: Kiểm Tra API Bằng Postman/Curl
```bash
curl -X GET "http://localhost:8080/api/branches/admin/1/detail" \
  -H "Content-Type: application/json"
```

**Response mong đợi:**
```json
{
  "branchId": 1,
  "branchName": "Chi nhánh Hà Nội",
  "branchCode": "HN001",
  "revenueDates": ["2025-10-20", "2025-10-21", "2025-10-22", ...],
  "revenueValues": [100000, 200000, 150000, ...],
  "bills": [...]
}
```

### Step 2: Test Frontend
1. Truy cập: `http://localhost:8080/admin/branch-management`
2. Nhấn nút **"Xem chi tiết"** của bất kỳ chi nhánh nào
3. Mở **Console (F12)** để xem logs
4. Kiểm tra xem modal có hiển thị không

### Step 3: Xem Logs Server
Trong console của Spring Boot, tìm:
```
INFO Getting branch detail for branchId: 1
INFO Revenue dates: [...], Revenue values: [...]
INFO Found 5 bills for branch 1
```

---

## 📝 Debugging Tips

### Thêm log vào HTML để track JavaScript:
```javascript
// Hàm tải chi tiết chi nhánh
function loadBranchDetail(branchId) {
    console.log('=== START LOADING BRANCH DETAIL ===');
    console.log('Branch ID:', branchId);
    
    fetch(`/api/branches/admin/${branchId}/detail`)
        .then(res => {
            console.log('Response Status:', res.status);
            console.log('Response Headers:', res.headers);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            return res.json();
        })
        .then(data => {
            console.log('=== DATA RECEIVED ===');
            console.log('Complete Data:', data);
            console.log('Branch Name:', data.branchName);
            console.log('Revenue Dates:', data.revenueDates);
            console.log('Revenue Values:', data.revenueValues);
            console.log('Bills Count:', data.bills ? data.bills.length : 0);
            // ... rest of code
        })
        .catch(err => {
            console.error('=== ERROR ===');
            console.error('Error Object:', err);
            console.error('Error Message:', err.message);
            console.error('Stack:', err.stack);
        });
}
```

---

## 🆘 Nếu Vẫn Gặp Lỗi

**Gửi cho tôi:**
1. Screenshot của lỗi trong Console (F12)
2. Response từ Network tab
3. Logs từ Spring Boot server
4. Kết quả của SQL query: `SELECT * FROM branch LIMIT 5; SELECT * FROM bill LIMIT 5;`

---

**Happy debugging! 🎉**
