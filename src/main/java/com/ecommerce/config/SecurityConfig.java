package com.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * SecurityConfig — Spring Security configuration.
 *
 * Authentication strategy:
 *   - Auth is managed MANUALLY via HTTP session attributes (not Spring Security principal).
 *     This is because we have 3 separate user tables (admins, buyers, sellers).
 *   - Role enforcement is done by SessionGuardInterceptor + manual null-checks in controllers.
 *   - Spring Security is kept only for: URL permitting, CSRF control, and logout support.
 *
 * Password strategy (DEV PHASE):
 *   - NoOpPasswordEncoder is registered as the PasswordEncoder bean.
 *   - This means encode() returns the raw string as-is, and matches() does a plain equals().
 *   - All three services (AdminService, BuyerService, SellerService) store and compare
 *     passwords in PLAIN TEXT — no hashing is performed.
 *   - NOTE: Replace with BCryptPasswordEncoder before production deployment.
 *
 * Access matrix:
 *   /seller/**  → SELLER_EMAIL session  (except /seller/login, /seller/register)
 *   /buyer/**   → BUYER_EMAIL session   (except /buyer/login, /buyer/register)
 *   /admin/**   → ADMIN_USER session    (except /admin/login, /admin/register)
 *   /           → public
 *   /uploads/** → public (product images)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * NoOpPasswordEncoder is intentional for this dev phase.
     * encode() = no-op (returns raw string), matches() = plain equals().
     */
    @SuppressWarnings("deprecation")
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // All URL-level auth is handled by SessionGuardInterceptor
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // Disable Spring Security's default login page — we use our own Thymeleaf forms
            .formLogin(form -> form.disable())
            // Disable HTTP Basic auth
            .httpBasic(basic -> basic.disable())
            // Support GET /logout to invalidate session and redirect to home
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // CSRF disabled — custom session approach.
            // Enable in production with proper CSRF tokens.
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
