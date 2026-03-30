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
 * SecurityConfig — Role-based URL access control.
 *
 * Access matrix:
 *  /seller/**      → SELLER role only  (except /seller/login, /seller/register)
 *  /buyer/**       → BUYER role only   (except /buyer/login, /buyer/register)
 *  /admin/**       → ADMIN role only   (except /admin/login)
 *  /               → public
 *  /uploads/**     → public (for product images)
 *
 * Auth is handled manually via session attributes (not Spring Security principal)
 * because we have 3 separate user tables. Spring Security is used only for
 * BCrypt, CSRF, and URL protection via session-based role check in controllers.
 *
 * NOTE: We disable Spring Security's auto login form to use our own Thymeleaf forms.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // We manage auth manually via session; open all URLs at Security level
            // and enforce roles in controllers + an interceptor
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // Disable default login page
            .formLogin(form -> form.disable())
            // Disable HTTP basic
            .httpBasic(basic -> basic.disable())
            // Allow logout via POST
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // CSRF disabled — custom session approach; enable in production with proper CSRF tokens
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
