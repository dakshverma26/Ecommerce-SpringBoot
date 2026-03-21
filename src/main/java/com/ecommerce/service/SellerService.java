package com.ecommerce.service;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.dto.SellerRegisterDto;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Seller;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    // ─── Registration ─────────────────────────────────────────────────────

    @Transactional
    public Seller register(SellerRegisterDto dto) {
        if (sellerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }
        String hash = passwordEncoder.encode(dto.getPassword());
        Seller seller = new Seller(dto.getName(), dto.getEmail(), hash);
        Seller saved = sellerRepository.save(seller);
        log.info("Seller registered: {}", saved.getEmail());
        return saved;
    }

    // ─── Authentication ────────────────────────────────────────────────────

    public Optional<Seller> authenticate(String email, String rawPassword) {
        return sellerRepository.findByEmail(email)
                .filter(seller -> passwordEncoder.matches(rawPassword, seller.getPasswordHash()));
    }

    // ─── Product CRUD ──────────────────────────────────────────────────────

    @Transactional
    public Product addProduct(ProductDto dto, MultipartFile image, String sellerEmail) {
        Seller seller = sellerRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found: " + sellerEmail));

        String imageName = "";
        String imagePath = "";
        if (image != null && !image.isEmpty()) {
            imageName = fileService.saveFile(image, sellerEmail);
            imagePath = "/uploads/products/" + sellerEmail + "/" + imageName;
        }

        String productName = dto.getName().replaceAll("\\s+", "").toUpperCase();
        Product product = new Product(productName, dto.getCategory(), dto.getQuantity(),
                imageName, imagePath, dto.getPrice(), dto.getExpiry(), seller);
        Product saved = productRepository.save(product);
        log.info("Product added: {} by seller {}", saved.getName(), sellerEmail);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsBySeller(String sellerEmail) {
        Seller seller = sellerRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found: " + sellerEmail));
        return productRepository.findBySeller(seller);
    }

    @Transactional
    public Product updateProduct(Long productId, ProductDto dto, MultipartFile image, String sellerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        // Ensure seller owns this product
        if (!product.getSeller().getEmail().equals(sellerEmail)) {
            throw new SecurityException("Unauthorized: you don't own this product");
        }

        product.setName(dto.getName().replaceAll("\\s+", "").toUpperCase());
        product.setCategory(dto.getCategory());
        product.setQuantity(dto.getQuantity());
        product.setPrice(dto.getPrice());
        product.setExpiry(dto.getExpiry());

        if (image != null && !image.isEmpty()) {
            String imageName = fileService.saveFile(image, sellerEmail);
            product.setImageName(imageName);
            product.setImagePath("/uploads/products/" + sellerEmail + "/" + imageName);
        }

        Product saved = productRepository.save(product);
        log.info("Product updated: id={} by seller {}", productId, sellerEmail);
        return saved;
    }

    @Transactional
    public void deleteProduct(Long productId, String sellerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (!product.getSeller().getEmail().equals(sellerEmail)) {
            throw new SecurityException("Unauthorized: you don't own this product");
        }

        productRepository.delete(product);
        log.info("Product deleted: id={} by seller {}", productId, sellerEmail);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<Seller> findByEmail(String email) {
        return sellerRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Seller> getAllSellers() {
        return sellerRepository.findAll();
    }

    @Transactional
    public void addRevenue(String sellerEmail, double amount) {
        Seller seller = sellerRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found: " + sellerEmail));
        seller.setTotalRevenue(seller.getTotalRevenue() + amount);
        sellerRepository.save(seller);
    }
}
