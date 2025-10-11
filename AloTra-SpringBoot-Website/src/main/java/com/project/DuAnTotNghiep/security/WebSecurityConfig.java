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
     */
    @Configuration
    public static class AppConfiguration {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

            http
                .csrf().disable()
                .authorizeRequests()

                    // ✅ Cho phép tất cả tài nguyên tĩnh để frontend load hoàn toàn
                    .antMatchers(
                        "/css/**", "/js/**", "/images/**", "/vendor/**",
                        "/fonts/**", "/plugins/**", "/static/**",
                        "/webjars/**", "/favicon.ico", "/error"
                    ).permitAll()

                    // ✅ Guest — có thể xem sản phẩm và mua hàng mà không cần login
                    .antMatchers(
                        "/", "/home/**", "/getproduct/**", "/getabout/**",
                        "/getcontact/**", "/user-login", "/register",
                        "/forgot-pass/**", "/verify-email/**",
                        "/shoping-cart/**", "/checkout/**" // 👈 Guest allowed
                    ).permitAll()

                    // ✅ User — các chức năng cá nhân hóa
                    .antMatchers(
                        "/profile/**", "/orders/**", "/payment/**",
                        "/comment/**", "/discount/**"
                    ).hasAnyRole("USER", "EMPLOYEE", "ADMIN")

                    // ✅ Employee — có thêm quyền quản lý shop riêng
                    .antMatchers(
                        "/vendor/**", "/shop/**", "/order-management/**",
                        "/promotion/**", "/revenue/**"
                    ).hasAnyRole("EMPLOYEE", "ADMIN")

                    // ✅ Admin — quyền cao nhất, quản lý toàn hệ thống
                    .antMatchers(
                        "/admin/**", "/management/**", "/system/**"
                    ).hasRole("ADMIN")

                    // ✅ Mặc định: các request khác đều được phép (frontend tự do)
                    .anyRequest().permitAll()

                .and()
                    // ✅ Cấu hình form login
                    .formLogin()
                        .loginPage("/user-login")
                        .loginProcessingUrl("/user_login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/", true)
                        .successHandler(successHandler())
                        .permitAll()

                .and()
                    // ✅ Cấu hình logout
                    .logout()
                        .logoutUrl("/user_logout")
                        .logoutSuccessUrl("/")
                        .permitAll()

                .and()
                    // ✅ Remember-me
                    .rememberMe()
                        .key("AbcDefgHijklmnOp_123456789")
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(7 * 24 * 60 * 60);

            // ✅ Cho phép iframe (cho console hoặc template nhúng)
            http.headers().frameOptions().disable();

            return http.build();
        }

        /**
         * ✅ Xử lý sau khi login thành công
         */
        @Bean
        public AuthenticationSuccessHandler successHandler() {
            SavedRequestAwareAuthenticationSuccessHandler handler =
                    new SavedRequestAwareAuthenticationSuccessHandler();
            handler.setDefaultTargetUrl("/");
            return handler;
        }
    }

    /**
     * ✅ Cần cho form login hoạt động
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * ✅ Mở quyền load static resource cho chắc chắn
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers(
            "/img/**", "/js/**", "/css/**", "/fonts/**", "/plugins/**",
            "/vendor/**", "/static/**", "/webjars/**", "/images/**",
            "/favicon.ico", "/error"
        );
    }
}
