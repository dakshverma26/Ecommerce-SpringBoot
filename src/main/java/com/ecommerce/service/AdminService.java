package com.ecommerce.service;

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

    public Optional<Admin> authenticate(String username, String rawPassword) {
        return adminRepository.findByUsername(username)
                .filter(admin -> passwordEncoder.matches(rawPassword, admin.getPasswordHash()));
    }

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
