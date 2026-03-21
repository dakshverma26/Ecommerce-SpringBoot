package com.ecommerce.service;

import com.ecommerce.dto.SellerRegisterDto;
import com.ecommerce.entity.Seller;
import com.ecommerce.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // rolls back after each test — keeps DB clean
class SellerServiceTest {

    @Autowired SellerService sellerService;
    @Autowired SellerRepository sellerRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private SellerRegisterDto dto;

    @BeforeEach
    void setUp() {
        dto = new SellerRegisterDto();
        dto.setName("Test Seller");
        dto.setEmail("testseller@test.com");
        dto.setPassword("secret123");
    }

    @Test
    @DisplayName("Seller registers successfully and password is hashed")
    void testRegisterSeller() {
        Seller saved = sellerService.register(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("testseller@test.com");
        assertThat(saved.getName()).isEqualTo("Test Seller");
        // Password must be BCrypt-hashed, not plain text
        assertThat(saved.getPasswordHash()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", saved.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("Duplicate email registration throws exception")
    void testDuplicateEmailThrowsException() {
        sellerService.register(dto);

        SellerRegisterDto dto2 = new SellerRegisterDto();
        dto2.setName("Another Seller");
        dto2.setEmail("testseller@test.com"); // same email
        dto2.setPassword("anotherpassword");

        assertThatThrownBy(() -> sellerService.register(dto2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("Authenticate with correct credentials returns seller")
    void testAuthenticateSuccess() {
        sellerService.register(dto);
        Optional<Seller> result = sellerService.authenticate("testseller@test.com", "secret123");
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("testseller@test.com");
    }

    @Test
    @DisplayName("Authenticate with wrong password returns empty")
    void testAuthenticateWrongPassword() {
        sellerService.register(dto);
        Optional<Seller> result = sellerService.authenticate("testseller@test.com", "wrongpassword");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Authenticate with non-existent email returns empty")
    void testAuthenticateNonExistentEmail() {
        Optional<Seller> result = sellerService.authenticate("nobody@test.com", "password");
        assertThat(result).isEmpty();
    }
}
