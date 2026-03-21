package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Product entity — replaces the legacy dynamic seller_EMAIL tables.
 * All products are now centralized with a FK to their seller.
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String category;

    @Min(0)
    @Column(nullable = false)
    private int quantity;

    private String imageName;
    private String imagePath;

    @Min(0)
    @Column(nullable = false)
    private int price;

    private LocalDate expiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    public Product(String name, String category, int quantity,
                   String imageName, String imagePath, int price,
                   LocalDate expiry, Seller seller) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.imageName = imageName;
        this.imagePath = imagePath;
        this.price = price;
        this.expiry = expiry;
        this.seller = seller;
    }
}
