package com.ecommerce.service;

import com.ecommerce.dto.CartItemDto;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final BuyerRepository buyerRepository;
    private final AdminConfirmationRepository confirmationRepository;
    private final SellerService sellerService;

    /**
     * Place an order from the cart.
     * - Creates Order + OrderItems records
     * - Decrements product quantity (inventory update)
     * - Credits seller revenue
     * - Creates AdminConfirmation (PENDING)
     */
    @Transactional
    public Order placeOrder(List<CartItemDto> cart, String buyerEmail, String razorpayOrderId, String razorpayPaymentId) {
        Buyer buyer = buyerRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found: " + buyerEmail));

        double total = cart.stream()
                .mapToDouble(item -> (double) item.getPrice() * item.getQuantity())
                .sum();

        Order order = new Order(buyer, total);
        order.setRazorpayOrderId(razorpayOrderId);
        order.setRazorpayPaymentId(razorpayPaymentId);
        order = orderRepository.save(order);

        List<OrderItem> items = new ArrayList<>();
        for (CartItemDto cartItem : cart) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItem.getProductId()));

            // Validate stock
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for: " + product.getName());
            }

            // Decrement inventory
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Credit seller revenue
            double revenue = (double) cartItem.getPrice() * cartItem.getQuantity();
            sellerService.addRevenue(product.getSeller().getEmail(), revenue);

            OrderItem item = new OrderItem(order, product, cartItem.getQuantity(), cartItem.getPrice());
            items.add(item);
        }

        order.setItems(items);

        // Create pending admin confirmation
        AdminConfirmation confirmation = new AdminConfirmation(order);
        confirmationRepository.save(confirmation);

        log.info("Order placed: orderId={} buyer={} total=₹{}", order.getId(), buyerEmail, total);
        return order;
    }

    /**
     * Admin confirms an order — marks it PROCESSED.
     */
    @Transactional
    public AdminConfirmation confirmOrder(Long confirmationId) {
        AdminConfirmation confirmation = confirmationRepository.findById(confirmationId)
                .orElseThrow(() -> new ResourceNotFoundException("Confirmation not found: " + confirmationId));

        confirmation.setStatus(AdminConfirmation.ConfirmationStatus.PROCESSED);
        confirmation.setProcessedAt(LocalDateTime.now());
        confirmation.getOrder().setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(confirmation.getOrder());
        AdminConfirmation saved = confirmationRepository.save(confirmation);

        log.info("Order confirmed by admin: confirmationId={}", confirmationId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByBuyer(String buyerEmail) {
        Buyer buyer = buyerRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found: " + buyerEmail));
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyer.getId());
    }
}
