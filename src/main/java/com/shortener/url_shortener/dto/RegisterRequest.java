package com.shortener.url_shortener.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username is Required!")
    @Size(min = 3, max = 21, message = "Username must be between 3 and 20 characters")
    private String username;

    @Email(message = "Please enter a valid Email!")
    @NotBlank(message = "Email is Required!")
    private String email;

    @NotBlank(message = "Password is required!")
    @Size(min = 7, message = "Password must be at least 7 characters")
    private String password;

    private Set<String> roles;
}