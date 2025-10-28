package com.project.DuAnTotNghiep.entity;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Account")
public class Account implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthDay;

    private String email;
    private String password;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    // ✅ Mặc định tài khoản không bị khóa
    @Column(nullable = false)
    private boolean isNonLocked = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roleId", nullable = false)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "branch_id", nullable = true)
    private Branch branch;

    // ⚠️ Giữ phương thức này để Spring không crash khi gọi isEnabled()
    // nhưng luôn trả true (vì DB không có cột enabled)
    public boolean isEnabled() {
        return true;
    }
}
