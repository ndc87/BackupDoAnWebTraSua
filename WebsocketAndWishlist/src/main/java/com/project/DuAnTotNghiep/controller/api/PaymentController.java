package com.project.DuAnTotNghiep.controller.api;

import com.project.DuAnTotNghiep.config.ConfigVNPay;
import com.project.DuAnTotNghiep.dto.Payment.PaymentResultDto;
import com.project.DuAnTotNghiep.dto.Order.OrderDto;
import com.project.DuAnTotNghiep.entity.Bill;
import com.project.DuAnTotNghiep.entity.Payment;
import com.project.DuAnTotNghiep.repository.BillRepository;
import com.project.DuAnTotNghiep.repository.PaymentRepository;
import com.project.DuAnTotNghiep.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final CartService cartService;
    private final ObjectMapper objectMapper;

    public PaymentController(PaymentRepository paymentRepository, BillRepository billRepository, CartService cartService, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;
        this.cartService = cartService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/payment-result")
    public String viewPaymentResult(HttpServletRequest request, Model model, HttpSession session) throws UnsupportedEncodingException {
        Map<String, String> fields = new HashMap<>();

        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        String signValue = ConfigVNPay.hashAllFields(fields);

        PaymentResultDto paymentResultDto = new PaymentResultDto();
        paymentResultDto.setTxnRef(fields.get("vnp_TxnRef"));
        paymentResultDto.setAmount(String.valueOf(Double.parseDouble(fields.get("vnp_Amount")) / 100));
        paymentResultDto.setBankCode(fields.get("vnp_BankCode"));
        paymentResultDto.setDatePay(fields.get("vnp_PayDate"));
        paymentResultDto.setResponseCode(fields.get("vnp_ResponseCode"));
        paymentResultDto.setTransactionStatus(fields.get("vnp_TransactionStatus"));

        model.addAttribute("result", paymentResultDto);

        if (signValue.equals(vnp_SecureHash)) {
            boolean checkOrderId = paymentRepository.existsByOrderId(paymentResultDto.getTxnRef());
            if (checkOrderId) {
                Payment paymentUpdate = paymentRepository.findByOrderId(paymentResultDto.getTxnRef());
                double amountFromVNPay = Double.parseDouble(paymentResultDto.getAmount());
                double amountFromDB = Double.parseDouble(paymentUpdate.getAmount());
                boolean checkAmount = amountFromVNPay == amountFromDB;
                boolean checkOrderStatus = paymentUpdate.getOrderStatus().equals("0");

                if (checkAmount) {
                    if (checkOrderStatus) {
                        if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {

                            try {
                                // ✅ BƯỚC 1: Lấy dataSend từ session (được gửi từ frontend qua sessionStorage)
                                String orderTempJson = (String) session.getAttribute("orderTemp");
                                if (orderTempJson == null) {
                                    model.addAttribute("status", "Không tìm thấy dữ liệu đơn hàng tạm thời!");
                                    model.addAttribute("paymentSuccess", false);
                                    return "user/payment-result";
                                }

                                // ✅ BƯỚC 2: Parse JSON thành OrderDto
                                OrderDto orderDto = objectMapper.readValue(orderTempJson, OrderDto.class);
                                orderDto.setOrderId(paymentResultDto.getTxnRef());

                             // ✅ BƯỚC 3: Gọi cartService.orderUser() để tạo Bill
                                cartService.orderUser(orderDto);
                                System.out.println("✅ Tạo đơn hàng thành công cho OrderId: " + paymentResultDto.getTxnRef());
                             
                                if (orderDto.getOrderDetailDtos() != null) {
                                    orderDto.getOrderDetailDtos().forEach(item -> {
                                        System.out.println("   • productDetailId=" + item.getProductDetailId() + ", quantity=" + item.getQuantity());
                                        if (item.getToppings() != null && !item.getToppings().isEmpty()) {
                                            item.getToppings().forEach(t ->
                                                System.out.println("      ↳ Topping: " + t.getName() + " - " + t.getPrice())
                                            );
                                        }
                                    });
                                } else {
                                    System.out.println("   (Không có sản phẩm)");
                                }
                                System.out.println("-----------------------------------------");

                                // 🔍 So sánh dữ liệu trong DB
                                Bill latestBill = billRepository.findTopByOrderByIdDesc();
                                if (latestBill != null) {
                                    System.out.println("🧾 Bill ID: " + latestBill.getId());
                                    System.out.println("💵 Số tiền lưu trong bill (backend tính): " + latestBill.getAmount());
                                }
                                Payment latestPayment = paymentRepository.findByOrderId(paymentResultDto.getTxnRef());
                                if (latestPayment != null) {
                                    System.out.println("💰 Số tiền VNPay lưu trong DB: " + latestPayment.getAmount());
                                }
                                System.out.println("=========================================");

                                // ✅ BƯỚC 4: Cập nhật payment trỏ đến Bill vừa tạo
                                Bill bill = billRepository.findTopByOrderByIdDesc();
                                if (bill != null) {
                                    paymentRepository.updateBillAndStatus(bill.getId(), paymentUpdate.getId());
                                    paymentUpdate.setBill(bill);
                                    paymentRepository.save(paymentUpdate);

                                    System.out.println("✅ Cập nhật bill_id = " + bill.getId() +
                                                       " với số tiền VNPay = " + paymentUpdate.getAmount() +
                                                       " cho payment_id = " + paymentUpdate.getId());
                                }

                                model.addAttribute("status", "Giao dịch thành công");
                                model.addAttribute("paymentSuccess", true);
                                model.addAttribute("orderId", paymentResultDto.getTxnRef());

                                // ✅ BƯỚC 5: Xóa dữ liệu tạm từ session
                                session.removeAttribute("orderTemp");

                            } catch (Exception e) {
                                e.printStackTrace();
                                model.addAttribute("status", "Lỗi khi xử lý đơn hàng: " + e.getMessage());
                                model.addAttribute("paymentSuccess", false);
                            }
                        } else {
                            model.addAttribute("status", "Giao dịch không thành công");
                            model.addAttribute("paymentSuccess", false);
                        }
                    } else {
                        model.addAttribute("status", "Đơn hàng đã được thanh toán");
                        model.addAttribute("paymentSuccess", false);
                    }
                } else {
                    model.addAttribute("status", "Số tiền không khớp");
                    model.addAttribute("paymentSuccess", false);
                }
            } else {
                model.addAttribute("status", "Mã giao dịch không tồn tại");
                model.addAttribute("paymentSuccess", false);
            }
        } else {
            model.addAttribute("status", "Invalid checksum");
            model.addAttribute("paymentSuccess", false);
        }

        return "user/payment-result";
    }
}