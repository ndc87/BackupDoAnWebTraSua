package com.project.DuAnTotNghiep.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	/**
	 * ✅ Mã hóa mật khẩu
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * ✅ Cấu hình chính cho phân quyền & bảo mật
	 * 
	 * PHÂN QUYỀN THEO YÊU CẦU ĐỒ ÁN:
	 * 
	 * 1. Guest (không cần đăng nhập): - Xem trang chủ với top 10 sản phẩm bán chạy
	 * - Xem sản phẩm theo danh mục - Đăng ký tài khoản
	 * 
	 * 2. User (ROLE_USER): - Tất cả chức năng Guest - Profile và quản lý địa chỉ
	 * nhận hàng - Giỏ hàng, thanh toán (COD, VNPAY) - Quản lý lịch sử mua hàng -
	 * Yêu thích và đánh giá sản phẩm - Sử dụng mã giảm giá
	 * 
	 * 3. Employee (ROLE_EMPLOYEE): - Tất cả chức năng User - Quản lý sản phẩm -
	 * Quản lý đơn hàng theo trạng thái - Tạo chương trình khuyến mãi - Thống kê
	 * doanh thu cơ bản
	 * 
	 * 4. Admin (ROLE_ADMIN): - Quản lý user - Quản lý toàn bộ sản phẩm - Quản lý
	 * danh mục, mã giảm giá - Thống kê doanh thu tại /admin/thong-ke-doanh-thu -
	 * Quản lý phương thức thanh toán
	 */
	@Configuration
	public static class AppConfiguration {
		@Bean
		public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
			http.csrf().disable().authorizeRequests()
					// ⚙️ Cho phép truy cập tài nguyên tĩnh (CSS, JS, images, ...)
					.antMatchers("/css/**", "/js/**", "/images/**", "/vendor/**", "/plugins/**", "/webjars/**",
							"/favicon.ico", "/error")
					.permitAll()

					// 🟢 Guest (public pages)
					.antMatchers("/", "/home/**", "/product/**", "/about/**", "/contact/**").permitAll()

					// 🔵 User
					.antMatchers("/profile/**", "/orders/**", "/checkout/**", "/comment/**", "/discount/**",
							"/favorite/**", "/cart/**", "/payment/**", "/history/**")
					.hasAnyRole("USER", "VENDOR", "ADMIN")

					// 🟣 Vendor (Seller)
					.antMatchers("/vendor/**", "/shop-management/**", "/order-management/**", "/promotion/**",
							"/revenue/**")
					.hasAnyRole("VENDOR", "ADMIN")

					// 🔴 Admin
					.antMatchers("/admin/**", "/management/**", "/system/**").hasRole("ADMIN")

					// Các request khác thì cho phép (để login form không bị chặn)
					.anyRequest().permitAll()

					.and().formLogin().loginPage("/user-login") // trang login
					.loginProcessingUrl("/user_login") // action form login
					.usernameParameter("email") // dùng email làm username
					.defaultSuccessUrl("/", true) // chuyển về trang chủ sau khi login
					.permitAll().and().logout().logoutUrl("/user_logout").logoutSuccessUrl("/").permitAll().and()
					.rememberMe().key("AbcDefgHijklmnOp_123456789").rememberMeParameter("remember-me")
					.tokenValiditySeconds(7 * 24 * 60 * 60);

			http.headers().frameOptions().disable();
			return http.build();
		}

		
	/**
	 * ✅ Xử lý sau khi login thành công
	 */
	@Bean
	public AuthenticationSuccessHandler successHandler() {
		SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
		handler.setDefaultTargetUrl("/");
		return handler;
	}

	/**
	 * ✅ Cần cho form login hoạt động
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	/**
	 * ✅ Mở quyền load static resource cho chắc chắn
	 */
	// Cho phép truy cập tài nguyên tĩnh mà không cần kiểm tra bảo mật
	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().antMatchers("/img/**", "/js/**", "/css/**", "/fonts/**", "/plugins/**",
				"/vendor/**", "/static/**", "/webjars/**", "/images/**", "/favicon.ico", "/error");
	}
}
}
