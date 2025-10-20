package com.project.DuAnTotNghiep.controller.api;

import com.project.DuAnTotNghiep.config.ConfigVNPay;
import com.project.DuAnTotNghiep.dto.Payment.PaymentResultDto;
import com.project.DuAnTotNghiep.entity.Bill;
import com.project.DuAnTotNghiep.entity.Payment;
import com.project.DuAnTotNghiep.repository.BillRepository;
import com.project.DuAnTotNghiep.repository.PaymentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
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

    public PaymentController(PaymentRepository paymentRepository, BillRepository billRepository) {
        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;
    }

    @GetMapping("/payment-result")
    public String viewPaymentResult(HttpServletRequest request, Model model) throws UnsupportedEncodingException {
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

                            // ✅ Lấy Bill mới nhất hoặc theo user (vì Guest cũng có Bill)
                            Bill bill = billRepository.findTopByOrderByIdDesc();

                            if (bill == null || bill.getId() == null) {
                                model.addAttribute("status", "Không tìm thấy hóa đơn để gán thanh toán!");
                                model.addAttribute("paymentSuccess", false);
                                System.err.println("⚠️ Không tìm thấy Bill nào trong DB để gán!");
                                return "user/payment-result";
                            }

                            // ✅ Chỉ cập nhật trực tiếp bằng SQL (không cần setBill)
                            paymentRepository.updateBillAndStatus(bill.getId(), paymentUpdate.getId());
                            System.out.println("✅ Cập nhật bill_id = " + bill.getId() + " cho payment_id = " + paymentUpdate.getId());

                            model.addAttribute("status", "Giao dịch thành công");
                            model.addAttribute("paymentSuccess", true);
                            model.addAttribute("orderId", paymentResultDto.getTxnRef());
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
