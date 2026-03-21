package com.ecommerce.service;

import com.ecommerce.dto.BuyerRegisterDto;
import com.ecommerce.entity.Buyer;
import com.ecommerce.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuyerService {

    private final BuyerRepository buyerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Buyer register(BuyerRegisterDto dto) {
        if (buyerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }
        String hash = passwordEncoder.encode(dto.getPassword());
        Buyer buyer = new Buyer(dto.getName(), dto.getEmail(), hash);
        buyer.setAddress(dto.getAddress());
        Buyer saved = buyerRepository.save(buyer);
        log.info("Buyer registered: {}", saved.getEmail());
        return saved;
    }

    public Optional<Buyer> authenticate(String email, String rawPassword) {
        return buyerRepository.findByEmail(email)
                .filter(b -> passwordEncoder.matches(rawPassword, b.getPasswordHash()));
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
