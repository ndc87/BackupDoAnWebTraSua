package com.project.DuAnTotNghiep.entity;

import com.project.DuAnTotNghiep.entity.enumClass.BillStatus;
import com.project.DuAnTotNghiep.entity.enumClass.InvoiceType;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bill") 
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bill implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String code;

    @Column(name = "promotion_price")
    private double promotionPrice;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BillStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type")
    private InvoiceType invoiceType;

    @Nationalized
    @Column(name = "billing_address")
    private String billingAddress;

    private Double amount;

    @Column(name = "return_status")
    private Boolean returnStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bill", orphanRemoval = true)
    private List<BillDetail> billDetail;

    @ManyToOne
    @JoinColumn(name = "discount_code_id")
    private DiscountCode discountCode;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;
}