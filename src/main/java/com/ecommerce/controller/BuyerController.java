package com.ecommerce.controller;

import com.ecommerce.dto.CartItemDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Product;
import com.ecommerce.service.BuyerService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.SellerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/buyer")
@RequiredArgsConstructor
public class BuyerController {

    private final ProductService productService;
    private final OrderService orderService;
    private final BuyerService buyerService;

    private static final String CART_SESSION_KEY = "BUYER_CART";

    // ─── Dashboard — Browse Products ────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        List<Product> products = productService.getAllAvailableProducts();
        model.addAttribute("products", products);
        model.addAttribute("buyerName", session.getAttribute("BUYER_NAME"));

        // Cart item count for nav badge
        List<CartItemDto> cart = getCart(session);
        model.addAttribute("cartCount", cart.size());
        return "buyer/dashboard";
    }

    // ─── Cart ─────────────────────────────────────────────────────────────
    /**
     * Add a product to the session cart.
     * NOTE: Session-based cart — simple and sufficient for this scope.
     * Future improvement: move to Redis or DB-backed cart in production.
     */
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttrs) {
        Product product = productService.getAllProducts().stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new com.ecommerce.exception.ResourceNotFoundException("Product not found"));

        if (quantity < 1 || quantity > product.getQuantity()) {
            redirectAttrs.addFlashAttribute("errorMsg",
                    "Invalid quantity. Available: " + product.getQuantity());
            return "redirect:/buyer/dashboard";
        }

        List<CartItemDto> cart = getCart(session);

        // Check if product already in cart — update quantity
        boolean found = false;
        for (CartItemDto item : cart) {
            if (item.getProductId().equals(productId)) {
                int newQty = Math.min(item.getQuantity() + quantity, product.getQuantity());
                item.setQuantity(newQty);
                found = true;
                break;
            }
        }

        if (!found) {
            CartItemDto item = new CartItemDto(
                    product.getId(),
                    product.getName(),
                    product.getSeller().getName(),
                    product.getCategory(),
                    product.getPrice(),
                    quantity,
                    product.getQuantity(),
                    product.getImagePath(),
                    product.getImageName()
            );
            cart.add(item);
        }

        session.setAttribute(CART_SESSION_KEY, cart);
        redirectAttrs.addFlashAttribute("successMsg", "Added to cart!");
        return "redirect:/buyer/dashboard";
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        List<CartItemDto> cart = getCart(session);
        int total = cart.stream().mapToInt(CartItemDto::getSubtotal).sum();
        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        model.addAttribute("buyerName", session.getAttribute("BUYER_NAME"));
        return "buyer/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttrs) {
        List<CartItemDto> cart = getCart(session);
        cart.removeIf(item -> item.getProductId().equals(productId));
        session.setAttribute(CART_SESSION_KEY, cart);
        redirectAttrs.addFlashAttribute("successMsg", "Item removed from cart.");
        return "redirect:/buyer/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttrs) {
        session.removeAttribute(CART_SESSION_KEY);
        redirectAttrs.addFlashAttribute("successMsg", "Cart cleared.");
        return "redirect:/buyer/cart";
    }

    // ─── Payment / Order ───────────────────────────────────────────────────
    @GetMapping("/payment")
    public String paymentPage(HttpSession session, Model model) {
        List<CartItemDto> cart = getCart(session);
        if (cart.isEmpty()) return "redirect:/buyer/cart";
        int total = cart.stream().mapToInt(CartItemDto::getSubtotal).sum();
        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        model.addAttribute("buyerName", session.getAttribute("BUYER_NAME"));
        return "buyer/payment";
    }

    @PostMapping("/order/place")
    public String placeOrder(HttpSession session, RedirectAttributes redirectAttrs) {
        String buyerEmail = (String) session.getAttribute("BUYER_EMAIL");
        List<CartItemDto> cart = getCart(session);

        if (cart.isEmpty()) {
            redirectAttrs.addFlashAttribute("errorMsg", "Cart is empty!");
            return "redirect:/buyer/cart";
        }

        try {
            Order order = orderService.placeOrder(cart, buyerEmail);
            session.removeAttribute(CART_SESSION_KEY); // clear cart after order
            redirectAttrs.addFlashAttribute("orderId", order.getId());
            redirectAttrs.addFlashAttribute("total", order.getTotalAmount());
            return "redirect:/buyer/order/success";
        } catch (Exception e) {
            log.error("Order placement failed: {}", e.getMessage());
            redirectAttrs.addFlashAttribute("errorMsg", "Order failed: " + e.getMessage());
            return "redirect:/buyer/cart";
        }
    }

    @GetMapping("/order/success")
    public String orderSuccess() { return "buyer/order-success"; }

    @GetMapping("/orders")
    public String myOrders(HttpSession session, Model model) {
        String buyerEmail = (String) session.getAttribute("BUYER_EMAIL");
        model.addAttribute("orders", orderService.getOrdersByBuyer(buyerEmail));
        model.addAttribute("buyerName", session.getAttribute("BUYER_NAME"));
        return "buyer/orders";
    }

    // ─── Helper ───────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private List<CartItemDto> getCart(HttpSession session) {
        List<CartItemDto> cart = (List<CartItemDto>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }
}
