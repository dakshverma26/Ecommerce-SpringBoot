package com.ecommerce.service;

import com.ecommerce.dto.BuyerRegisterDto;
import com.ecommerce.entity.Buyer;
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
@Transactional
class BuyerServiceTest {

    @Autowired BuyerService buyerService;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Buyer registers and password is hashed")
    void testRegisterBuyer() {
        BuyerRegisterDto dto = new BuyerRegisterDto();
        dto.setName("Test Buyer");
        dto.setEmail("testbuyer@test.com");
        dto.setPassword("mypassword");
        dto.setAddress("123 Main St");

        Buyer saved = buyerService.register(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("testbuyer@test.com");
        assertThat(passwordEncoder.matches("mypassword", saved.getPasswordHash())).isTrue();
        assertThat(saved.getAddress()).isEqualTo("123 Main St");
    }

    @Test
    @DisplayName("Duplicate buyer email throws exception")
    void testDuplicateEmail() {
        BuyerRegisterDto dto = new BuyerRegisterDto();
        dto.setName("Buyer A");
        dto.setEmail("duplicate@test.com");
        dto.setPassword("pass123");
        buyerService.register(dto);

        BuyerRegisterDto dto2 = new BuyerRegisterDto();
        dto2.setName("Buyer B");
        dto2.setEmail("duplicate@test.com");
        dto2.setPassword("pass456");

        assertThatThrownBy(() -> buyerService.register(dto2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Buyer authentication with correct credentials succeeds")
    void testAuthenticate() {
        BuyerRegisterDto dto = new BuyerRegisterDto();
        dto.setName("Auth Buyer");
        dto.setEmail("auth@test.com");
        dto.setPassword("auth123");
        buyerService.register(dto);

        Optional<Buyer> result = buyerService.authenticate("auth@test.com", "auth123");
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Buyer authentication with wrong password fails")
    void testAuthenticateFail() {
        BuyerRegisterDto dto = new BuyerRegisterDto();
        dto.setName("Fail Buyer");
        dto.setEmail("fail@test.com");
        dto.setPassword("correctpass");
        buyerService.register(dto);

        Optional<Buyer> result = buyerService.authenticate("fail@test.com", "wrongpass");
        assertThat(result).isEmpty();
    }
}
