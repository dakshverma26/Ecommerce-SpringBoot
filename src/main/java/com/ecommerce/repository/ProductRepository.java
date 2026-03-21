package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySeller(Seller seller);
    List<Product> findBySellerId(Long sellerId);

    // Only show products with available stock
    List<Product> findByQuantityGreaterThan(int quantity);

    @Query("SELECT p FROM Product p JOIN FETCH p.seller WHERE p.quantity > 0")
    List<Product> findAllAvailableWithSeller();
}
