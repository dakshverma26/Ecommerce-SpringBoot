package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_confirmations")
@Data
@NoArgsConstructor
public class AdminConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConfirmationStatus status = ConfirmationStatus.PENDING;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime processedAt;

    public enum ConfirmationStatus {
        PENDING, PROCESSED
    }

    public AdminConfirmation(Order order) {
        this.order = order;
        this.status = ConfirmationStatus.PENDING;
    }
}
