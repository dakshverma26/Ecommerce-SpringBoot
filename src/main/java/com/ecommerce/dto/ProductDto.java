package com.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class ProductDto {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @Min(value = 1, message = "Price must be at least 1")
    private int price;

    @NotNull(message = "Expiry date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiry;

    // Not validated as required — update operations may not re-upload image
    private MultipartFile image;
}
