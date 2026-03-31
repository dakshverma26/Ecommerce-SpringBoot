package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin entity.
 * Passwords are stored in PLAIN TEXT for this development phase.
 * Do NOT use BCrypt or any PasswordEncoder in production without updating this.
 */
@Entity
@Table(name = "admins")
@Data
@NoArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    // Plain-text password. Column kept as 'password_hash' in DB for backward compat.
    @Column(name = "password_hash", nullable = false)
    private String password;

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
