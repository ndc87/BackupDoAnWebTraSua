package com.project.DuAnTotNghiep.entity;

import com.project.DuAnTotNghiep.entity.enumClass.PaymentMethodName;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "payment_method") // ✅ đúng với tên thật trong DB
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethod implements Serializable {
    @Id
    private Long id;

    @Nationalized
    @Enumerated(EnumType.STRING)
    private PaymentMethodName name;

    private int status;
}
