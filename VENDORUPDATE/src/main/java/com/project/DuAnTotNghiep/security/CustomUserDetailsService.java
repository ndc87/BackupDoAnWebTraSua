package com.project.DuAnTotNghiep.security;

import com.project.DuAnTotNghiep.entity.Account;
import com.project.DuAnTotNghiep.repository.AccountRepository;
import com.project.DuAnTotNghiep.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository; // ✅ sửa đúng tên biến
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 [CustomUserDetailsService] Đang xác thực user: " + username);

        Account account = accountRepository.findByEmail(username);

        if (account != null) {
            System.out.println("✅ [CustomUserDetailsService] Đăng nhập thành công:");
            System.out.println("   📧 Email: " + account.getEmail());
            System.out.println("   🧩 Role: " + (account.getRole() != null ? account.getRole().getName() : "NULL"));
            System.out.println("   🔒 Locked: " + !account.isNonLocked());
            System.out.println("   🕒 Enabled: " + account.isEnabled());
            return new CustomUserDetails(account);
        }

        System.out.println("❌ [CustomUserDetailsService] Không tìm thấy tài khoản có email: " + username);
        throw new UsernameNotFoundException("Không tìm thấy tài khoản có email: " + username);
    }
}
