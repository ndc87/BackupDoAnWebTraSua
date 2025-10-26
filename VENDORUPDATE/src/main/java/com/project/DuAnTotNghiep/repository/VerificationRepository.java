package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByAccount(Account account);
    Optional<VerificationCode> findByCodeAndAccount(String code, Account account);
    Optional<VerificationCode> findByCode(String code); // ✅ giữ lại dòng này cho verifyCode()
}