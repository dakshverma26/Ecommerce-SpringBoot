package com.ecommerce.service;

import com.ecommerce.dto.AdminRegisterDto;
import com.ecommerce.entity.Admin;
import com.ecommerce.entity.AdminConfirmation;
import com.ecommerce.repository.AdminConfirmationRepository;
import com.ecommerce.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * AdminService — manages admin authentication and registration.
 *
 * NOTE: Passwords are stored and compared in PLAIN TEXT for this dev phase.
 * No PasswordEncoder is used intentionally.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final AdminConfirmationRepository confirmationRepository;

    // ─── Authentication ────────────────────────────────────────────────────

    /**
     * Authenticate admin by username and plain-text password.
     * Returns empty if username not found or password does not match.
     */
    public Optional<Admin> authenticate(String username, String rawPassword) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return Optional.empty();
        }
        return adminRepository.findByUsername(username)
                .filter(admin -> rawPassword.equals(admin.getPassword()));
    }

    // ─── Registration ──────────────────────────────────────────────────────

    @Transactional
    public Admin register(AdminRegisterDto dto) {
        if (adminRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + dto.getUsername());
        }
        // Store password in plain text — no encoding
        Admin admin = new Admin(dto.getUsername(), dto.getPassword());
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
