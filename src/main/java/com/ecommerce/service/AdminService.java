package com.ecommerce.service;

import com.ecommerce.dto.AdminRegisterDto;
import com.ecommerce.entity.Admin;
import com.ecommerce.entity.AdminConfirmation;
import com.ecommerce.repository.AdminConfirmationRepository;
import com.ecommerce.repository.AdminRepository;
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
public class AdminService {

    private final AdminRepository adminRepository;
    private final AdminConfirmationRepository confirmationRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Authentication ────────────────────────────────────────────────────

    public Optional<Admin> authenticate(String username, String rawPassword) {
        // NoOpPasswordEncoder: matches() does a plain equals() comparison
        return adminRepository.findByUsername(username)
                .filter(admin -> passwordEncoder.matches(rawPassword, admin.getPasswordHash()));
    }

    // ─── Registration ──────────────────────────────────────────────────────

    @Transactional
    public Admin register(AdminRegisterDto dto) {
        if (adminRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + dto.getUsername());
        }
        // NoOpPasswordEncoder: encode() returns the password as-is (plain text)
        Admin admin = new Admin(dto.getUsername(), passwordEncoder.encode(dto.getPassword()));
        Admin saved = adminRepository.save(admin);
        log.info("Admin registered: {}", saved.getUsername());
        return saved;
    }

    // ─── Confirmations ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AdminConfirmation> getAllConfirmations() {
        return confirmationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<AdminConfirmation> getPendingConfirmations() {
        return confirmationRepository
                .findByStatusOrderByCreatedAtDesc(AdminConfirmation.ConfirmationStatus.PENDING);
    }
}

