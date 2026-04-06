package com.ecommerce.controller;

import com.ecommerce.dto.CartItemDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Product;
import com.ecommerce.service.BuyerService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.RazorpayService;
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
    private final RazorpayService razorpayService;

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
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttrs) {
        // Guard: invalid quantity
        if (productId == null) {
            redirectAttrs.addFlashAttribute("errorMsg", "Invalid product.");
            return "redirect:/buyer/dashboard";
        }

        Product product = productService.getAllAvailableProducts().stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (product == null) {
            redirectAttrs.addFlashAttribute("errorMsg", "Product not found or out of stock.");
            return "redirect:/buyer/dashboard";
        }

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

    // ─── Checkout — Payment Bypassed ───────────────────────────────────────
    /**
     * Payment gateway is DISABLED for this phase.
     * The /buyer/payment page now shows an order summary and a direct
     * "Place Order" button — no payment verification is performed.
     */
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

    @PostMapping("/order/verify-and-place")
    public String verifyAndPlaceOrder(@RequestParam("razorpay_payment_id") String paymentId,
                                      @RequestParam("razorpay_order_id") String orderId,
                                      @RequestParam("razorpay_signature") String signature,
                                      HttpSession session, RedirectAttributes redirectAttrs) {
        String buyerEmail = (String) session.getAttribute("BUYER_EMAIL");

        if (buyerEmail == null || buyerEmail.isBlank()) {
            redirectAttrs.addFlashAttribute("errorMsg", "Your session has expired. Please login again.");
            return "redirect:/buyer/login?error=session";
        }

        List<CartItemDto> cart = getCart(session);
        if (cart.isEmpty()) {
            redirectAttrs.addFlashAttribute("errorMsg", "Cart is empty!");
            return "redirect:/buyer/cart";
        }

        // Verify Razorpay Payment Signature
        boolean isValid = razorpayService.verifySignature(orderId, paymentId, signature);
        if (!isValid) {
            log.warn("Payment signature verification failed for buyer: {}", buyerEmail);
            redirectAttrs.addFlashAttribute("errorMsg", "Payment verification failed. Please try again or contact support.");
            return "redirect:/buyer/payment";
        }

        try {
            Order order = orderService.placeOrder(cart, buyerEmail, orderId, paymentId);
            session.removeAttribute(CART_SESSION_KEY);
            redirectAttrs.addFlashAttribute("orderId", order.getId());
            redirectAttrs.addFlashAttribute("total", order.getTotalAmount());
            return "redirect:/buyer/order/success";
        } catch (IllegalStateException e) {
            log.warn("Order placement — stock issue: {}", e.getMessage());
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/buyer/cart";
        } catch (Exception e) {
            log.error("Order placement failed: {}", e.getMessage());
            redirectAttrs.addFlashAttribute("errorMsg", "Order could not be placed. Please try again.");
            return "redirect:/buyer/cart";
        }
    }

    @GetMapping("/order/success")
    public String orderSuccess() { return "buyer/order-success"; }

    @GetMapping("/orders")
    public String myOrders(HttpSession session, Model model) {
        String buyerEmail = (String) session.getAttribute("BUYER_EMAIL");
        if (buyerEmail == null) {
            return "redirect:/buyer/login?error=session";
        }
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
