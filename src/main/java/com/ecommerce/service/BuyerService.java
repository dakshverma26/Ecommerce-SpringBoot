package com.ecommerce.service;

import com.ecommerce.dto.BuyerRegisterDto;
import com.ecommerce.entity.Buyer;
import com.ecommerce.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * BuyerService — manages buyer registration and authentication.
 *
 * NOTE: Passwords are stored and compared in PLAIN TEXT for this dev phase.
 * No PasswordEncoder is used intentionally.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuyerService {

    private final BuyerRepository buyerRepository;

    @Transactional
    public Buyer register(BuyerRegisterDto dto) {
        if (buyerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }
        // Store password in plain text — no encoding
        Buyer buyer = new Buyer(dto.getName(), dto.getEmail(), dto.getPassword());
        buyer.setAddress(dto.getAddress());
        Buyer saved = buyerRepository.save(buyer);
        log.info("Buyer registered: {}", saved.getEmail());
        return saved;
    }

    /**
     * Authenticate buyer by email and plain-text password.
     */
    public Optional<Buyer> authenticate(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return Optional.empty();
        }
        return buyerRepository.findByEmail(email)
                .filter(b -> rawPassword.equals(b.getPassword()));
    }

    @Transactional(readOnly = true)
    public Optional<Buyer> findByEmail(String email) {
        return buyerRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Buyer> getAllBuyers() {
        return buyerRepository.findAll();
    }
}
