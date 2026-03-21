package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyer(Buyer buyer);
    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
}
