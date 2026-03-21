package com.ecommerce.repository;

import com.ecommerce.entity.AdminConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminConfirmationRepository extends JpaRepository<AdminConfirmation, Long> {
    List<AdminConfirmation> findByStatusOrderByCreatedAtDesc(AdminConfirmation.ConfirmationStatus status);
    List<AdminConfirmation> findAllByOrderByCreatedAtDesc();
}
