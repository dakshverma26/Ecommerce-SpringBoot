package com.ecommerce.controller;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Product;
import com.ecommerce.service.SellerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String sellerEmail = (String) session.getAttribute("SELLER_EMAIL");
        String sellerName  = (String) session.getAttribute("SELLER_NAME");

        // Guard: session may be null (interceptor should catch first, but defensive)
        if (sellerEmail == null) {
            return "redirect:/seller/login?error=session";
        }

        List<Product> products = sellerService.getProductsBySeller(sellerEmail);
        model.addAttribute("sellerName", sellerName);
        model.addAttribute("products", products);
        model.addAttribute("productCount", products.size());
        double revenue = sellerService.findByEmail(sellerEmail)
                .map(s -> s.getTotalRevenue()).orElse(0.0);
        model.addAttribute("revenue", revenue);
        return "seller/dashboard";
    }

    @GetMapping("/products")
    public String listProducts(HttpSession session, Model model) {
        String sellerEmail = (String) session.getAttribute("SELLER_EMAIL");
        if (sellerEmail == null) {
            return "redirect:/seller/login?error=session";
        }
        model.addAttribute("products", sellerService.getProductsBySeller(sellerEmail));
        model.addAttribute("sellerName", session.getAttribute("SELLER_NAME"));
        return "seller/products";
    }

    @GetMapping("/product/add")
    public String addProductPage(Model model) {
        model.addAttribute("productDto", new ProductDto());
        return "seller/add-product";
    }

    @PostMapping("/product/add")
    public String addProduct(@Valid @ModelAttribute("productDto") ProductDto dto,
                             BindingResult result,
                             @RequestParam("image") MultipartFile image,
                             HttpSession session,
                             RedirectAttributes redirectAttrs,
                             Model model) {
        if (result.hasErrors()) return "seller/add-product";

        String sellerEmail = (String) session.getAttribute("SELLER_EMAIL");
        if (sellerEmail == null) {
            return "redirect:/seller/login?error=session";
        }

        try {
            sellerService.addProduct(dto, image, sellerEmail);
            redirectAttrs.addFlashAttribute("successMsg", "Product added successfully!");
            return "redirect:/seller/products";
        } catch (Exception e) {
            log.error("Failed to add product: {}", e.getMessage());
            model.addAttribute("errorMsg", "Failed to add product: " + e.getMessage());
            return "seller/add-product";
        }
    }

    @GetMapping("/product/edit/{id}")
    public String editProductPage(@PathVariable Long id, HttpSession session, Model model) {
        String sellerEmail = (String) session.getAttribute("SELLER_EMAIL");
        if (sellerEmail == null) {
            return "redirect:/seller/login?error=session";
        }
        Product product = sellerService.getProductById(id);
        // Ensure seller owns this product
        if (!product.getSeller().getEmail().equals(sellerEmail)) {
            return "redirect:/seller/products";
        }
        ProductDto dto = new ProductDto();
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setQuantity(product.getQuantity());
        dto.setPrice(product.getPrice());
        dto.setExpiry(product.getExpiry());
        model.addAttribute("productDto", dto);
        model.addAttribute("productId", id);
        model.addAttribute("existingImagePath", product.getImagePath());
        return "seller/edit-product";
    }

    @PostMapping("/product/update/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("productDto") ProductDto dto,
                                BindingResult result,
                                @RequestParam(value = "image", required = false) MultipartFile image,
                                HttpSession session,
                                RedirectAttributes redirectAttrs,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("productId", id);
            return "seller/edit-product";
        }
        String sellerEmail = (String) session.getAttribute("SELLER_EMAIL");
        if (sellerEmail == null) {
            return "redirect:/seller/login?error=session";
        }
        try {
            sellerService.updateProduct(id, dto, image, sellerEmail);
            redirectAttrs.addFlashAttribute("successMsg", "Product updated successfully!");
            return "redirect:/seller/products";
        } catch (SecurityException e) {
            log.warn("Unauthorized product update attempt for product {} by {}", id, sellerEmail);
            redirectAttrs.addFlashAttribute("errorMsg", "Unauthorized: you don't own this product.");
            return "redirect:/seller/products";
        } catch (Exception e) {
            log.error("Failed to update product {}: {}", id, e.getMessage());
            model.addAttribute("errorMsg", "Update failed: " + e.getMessage());
            model.addAttribute("productId", id);
            return "seller/edit-product";
        }
    }

    @PostMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttrs) {
        String sellerEmail = (String) session.getAttribute("SELLER_EMAIL");
        if (sellerEmail == null) {
            return "redirect:/seller/login?error=session";
        }
        try {
            sellerService.deleteProduct(id, sellerEmail);
            redirectAttrs.addFlashAttribute("successMsg", "Product deleted.");
        } catch (SecurityException e) {
            log.warn("Unauthorized delete attempt for product {} by {}", id, sellerEmail);
            redirectAttrs.addFlashAttribute("errorMsg", "Unauthorized: you don't own this product.");
        } catch (Exception e) {
            log.error("Failed to delete product {}: {}", id, e.getMessage());
            redirectAttrs.addFlashAttribute("errorMsg", "Failed to delete product.");
        }
        return "redirect:/seller/products";
    }
}
