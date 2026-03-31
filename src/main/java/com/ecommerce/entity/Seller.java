package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seller entity.
 * Passwords are stored in PLAIN TEXT for this development phase.
 */
@Entity
@Table(name = "sellers")
@Data
@NoArgsConstructor
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    // Plain-text password. Column kept as 'password_hash' in DB for backward compat.
    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(nullable = false)
    private double totalRevenue = 0.0;

    @Column(nullable = false)
    private boolean approved = true; // default true; admin can disable

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

    public Seller(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
