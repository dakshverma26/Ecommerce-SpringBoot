package com.ecommerce.controller;

import com.ecommerce.entity.AdminConfirmation;
import com.ecommerce.entity.Seller;
import com.ecommerce.service.AdminService;
import com.ecommerce.service.BuyerService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.SellerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final SellerService sellerService;
    private final BuyerService buyerService;
    private final OrderService orderService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        model.addAttribute("adminUser", session.getAttribute("ADMIN_USER"));
        long pendingCount = adminService.getPendingConfirmations().size();
        long sellerCount = sellerService.getAllSellers().size();
        long buyerCount = buyerService.getAllBuyers().size();
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("sellerCount", sellerCount);
        model.addAttribute("buyerCount", buyerCount);
        return "admin/dashboard";
    }

    @GetMapping("/confirmations")
    public String confirmations(Model model, HttpSession session) {
        model.addAttribute("adminUser", session.getAttribute("ADMIN_USER"));
        model.addAttribute("confirmations", adminService.getAllConfirmations());
        return "admin/confirmations";
    }

    @PostMapping("/confirm/{id}")
    public String confirmOrder(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            orderService.confirmOrder(id);
            redirectAttrs.addFlashAttribute("successMsg", "Order #" + id + " confirmed successfully.");
        } catch (Exception e) {
            log.error("Failed to confirm order {}: {}", id, e.getMessage());
            redirectAttrs.addFlashAttribute("errorMsg", "Failed to confirm: " + e.getMessage());
        }
        return "redirect:/admin/confirmations";
    }

    @GetMapping("/sellers")
    public String sellers(Model model, HttpSession session) {
        model.addAttribute("adminUser", session.getAttribute("ADMIN_USER"));
        model.addAttribute("sellers", sellerService.getAllSellers());
        return "admin/sellers";
    }

    @GetMapping("/buyers")
    public String buyers(Model model, HttpSession session) {
        model.addAttribute("adminUser", session.getAttribute("ADMIN_USER"));
        model.addAttribute("buyers", buyerService.getAllBuyers());
        return "admin/buyers";
    }
}
