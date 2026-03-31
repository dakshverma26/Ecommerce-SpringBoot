package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Buyer entity.
 * Passwords are stored in PLAIN TEXT for this development phase.
 */
@Entity
@Table(name = "buyers")
@Data
@NoArgsConstructor
public class Buyer {

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

    private String address;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    public Buyer(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
