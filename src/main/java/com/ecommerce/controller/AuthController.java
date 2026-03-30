package com.ecommerce.controller;

import com.ecommerce.dto.AdminRegisterDto;
import com.ecommerce.dto.BuyerRegisterDto;
import com.ecommerce.dto.LoginDto;
import com.ecommerce.dto.SellerRegisterDto;
import com.ecommerce.entity.Admin;
import com.ecommerce.entity.Buyer;
import com.ecommerce.entity.Seller;
import com.ecommerce.service.AdminService;
import com.ecommerce.service.BuyerService;
import com.ecommerce.service.SellerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final SellerService sellerService;
    private final BuyerService buyerService;
    private final AdminService adminService;

    // ─── Home ─────────────────────────────────────────────────────────────
    @GetMapping("/")
    public String home() { return "index"; }

    // ─── Seller Auth ───────────────────────────────────────────────────────
    @GetMapping("/seller/login")
    public String sellerLoginPage(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("loginDto", new LoginDto());
        if ("session".equals(error)) model.addAttribute("errorMsg", "Session expired. Please login again.");
        return "seller/login";
    }

    @PostMapping("/seller/login")
    public String sellerLogin(@Valid @ModelAttribute("loginDto") LoginDto dto,
                              BindingResult result, HttpSession session, Model model) {
        if (result.hasErrors()) return "seller/login";

        Optional<Seller> seller = sellerService.authenticate(dto.getEmail(), dto.getPassword());
        if (seller.isPresent()) {
            session.setAttribute("SELLER_EMAIL", seller.get().getEmail());
            session.setAttribute("SELLER_NAME", seller.get().getName());
            log.info("Seller logged in: {}", seller.get().getEmail());
            return "redirect:/seller/dashboard";
        }
        model.addAttribute("errorMsg", "Invalid email or password");
        return "seller/login";
    }

    @GetMapping("/seller/register")
    public String sellerRegisterPage(Model model) {
        model.addAttribute("sellerDto", new SellerRegisterDto());
        return "seller/register";
    }

    @PostMapping("/seller/register")
    public String sellerRegister(@Valid @ModelAttribute("sellerDto") SellerRegisterDto dto,
                                 BindingResult result, RedirectAttributes redirectAttrs, Model model) {
        if (result.hasErrors()) return "seller/register";
        try {
            sellerService.register(dto);
            redirectAttrs.addFlashAttribute("successMsg", "Registration successful! Please login.");
            return "redirect:/seller/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "seller/register";
        }
    }

    // ─── Buyer Auth ────────────────────────────────────────────────────────
    @GetMapping("/buyer/login")
    public String buyerLoginPage(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("loginDto", new LoginDto());
        if ("session".equals(error)) model.addAttribute("errorMsg", "Session expired. Please login again.");
        return "buyer/login";
    }

    @PostMapping("/buyer/login")
    public String buyerLogin(@Valid @ModelAttribute("loginDto") LoginDto dto,
                             BindingResult result, HttpSession session, Model model) {
        if (result.hasErrors()) return "buyer/login";

        Optional<Buyer> buyer = buyerService.authenticate(dto.getEmail(), dto.getPassword());
        if (buyer.isPresent()) {
            session.setAttribute("BUYER_EMAIL", buyer.get().getEmail());
            session.setAttribute("BUYER_NAME", buyer.get().getName());
            log.info("Buyer logged in: {}", buyer.get().getEmail());
            return "redirect:/buyer/dashboard";
        }
        model.addAttribute("errorMsg", "Invalid email or password");
        return "buyer/login";
    }

    @GetMapping("/buyer/register")
    public String buyerRegisterPage(Model model) {
        model.addAttribute("buyerDto", new BuyerRegisterDto());
        return "buyer/register";
    }

    @PostMapping("/buyer/register")
    public String buyerRegister(@Valid @ModelAttribute("buyerDto") BuyerRegisterDto dto,
                                BindingResult result, RedirectAttributes redirectAttrs, Model model) {
        if (result.hasErrors()) return "buyer/register";
        try {
            buyerService.register(dto);
            redirectAttrs.addFlashAttribute("successMsg", "Registration successful! Please login.");
            return "redirect:/buyer/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "buyer/register";
        }
    }

    // ─── Admin Auth ────────────────────────────────────────────────────────
    @GetMapping("/admin/login")
    public String adminLoginPage(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("loginDto", new LoginDto());
        if ("session".equals(error)) model.addAttribute("errorMsg", "Session expired. Please login again.");
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@Valid @ModelAttribute("loginDto") LoginDto dto,
                             BindingResult result, HttpSession session, Model model) {
        if (result.hasErrors()) return "admin/login";

        Optional<Admin> admin = adminService.authenticate(dto.getEmail(), dto.getPassword());
        if (admin.isPresent()) {
            session.setAttribute("ADMIN_USER", admin.get().getUsername());
            log.info("Admin logged in: {}", admin.get().getUsername());
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("errorMsg", "Invalid username or password");
        return "admin/login";
    }

    @GetMapping("/admin/register")
    public String adminRegisterPage(Model model) {
        model.addAttribute("adminDto", new AdminRegisterDto());
        return "admin/register";
    }

    @PostMapping("/admin/register")
    public String adminRegister(@Valid @ModelAttribute("adminDto") AdminRegisterDto dto,
                                BindingResult result, RedirectAttributes redirectAttrs, Model model) {
        if (result.hasErrors()) return "admin/register";
        try {
            adminService.register(dto);
            redirectAttrs.addFlashAttribute("successMsg", "Admin account created! Please login.");
            return "redirect:/admin/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "admin/register";
        }
    }

    // ─── Logout ────────────────────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
