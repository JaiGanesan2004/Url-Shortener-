package com.shortener.url_shortener.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
