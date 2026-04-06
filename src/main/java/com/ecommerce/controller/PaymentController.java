package com.ecommerce.controller;

import com.ecommerce.dto.CartItemDto;
import com.ecommerce.service.RazorpayService;
import com.razorpay.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final RazorpayService razorpayService;

    @PostMapping(value = "/create-order", produces = "application/json")
    public ResponseEntity<?> createOrder(HttpSession session) {
        try {
            @SuppressWarnings("unchecked")
            List<CartItemDto> cart = (List<CartItemDto>) session.getAttribute("BUYER_CART");
            if (cart == null || cart.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
            }

            double total = cart.stream()
                    .mapToDouble(item -> (double) item.getPrice() * item.getQuantity())
                    .sum();

            Order order = razorpayService.createOrder(total);
            return ResponseEntity.ok(order.toString());
        } catch (Exception e) {
            log.error("Error creating Razorpay order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create order"));
        }
    }
}
