package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {

    @NotBlank(message = "Email/Username is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
