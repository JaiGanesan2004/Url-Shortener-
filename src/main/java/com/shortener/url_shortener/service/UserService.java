package com.shortener.url_shortener.service;

import com.shortener.url_shortener.dto.LoginRequest;
import com.shortener.url_shortener.models.User;
import com.shortener.url_shortener.repository.UserRepository;
import com.shortener.url_shortener.security.jwt.JwtAuthenticationResponse;
import com.shortener.url_shortener.security.jwt.JwtUtils;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepo;
    private AuthenticationManager authManager;
    private JwtUtils jwtUtils;

    @Retry(name = "CompanyBreaker")
    public User registerUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest){
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails);
        return new JwtAuthenticationResponse(jwt);

    }

    public User findByUsername(String name) {
        return userRepo.findByUsername(name).orElseThrow(() -> new UsernameNotFoundException("Username not found with username: " + name + "!"));
    }
}
