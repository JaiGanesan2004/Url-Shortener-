package com.shortener.url_shortener.controllers;

import com.shortener.url_shortener.dto.ClickEventDTO;
import com.shortener.url_shortener.dto.ShortenRequest;
import com.shortener.url_shortener.dto.UrlMappingDTO;
import com.shortener.url_shortener.models.User;
import com.shortener.url_shortener.service.UrlMappingService;
import com.shortener.url_shortener.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@AllArgsConstructor
public class UrlMappingController {
    private UrlMappingService urlMappingServ;
    private UserService userServ;

    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDTO> createShortUrl(@Valid @RequestBody ShortenRequest request, Principal principal){
        User user = userServ.findByUsername(principal.getName());
        UrlMappingDTO urlMappingDTO = urlMappingServ.createShortUrl(request.getOriginalUrl(), user);
        return ResponseEntity.ok(urlMappingDTO);
    }

    @GetMapping("/myurls")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<UrlMappingDTO>> getUserUrl(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        User user = userServ.findByUsername(principal.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<UrlMappingDTO> urls = urlMappingServ.getUrlsByUser(user, pageable);
        return ResponseEntity.ok(urls);
    }

    @GetMapping("/analytics/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ClickEventDTO>>  getUrlAnalytics(@PathVariable String shortUrl, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        List<ClickEventDTO> clickEventsDto = urlMappingServ.getClickEventsByDate(shortUrl, start, end);
        return ResponseEntity.ok(clickEventsDto);
    }

    @GetMapping("/totalClicks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<LocalDate, Long>> getTotalClicksByDate(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate, Principal principal){
        User user = userServ.findByUsername(principal.getName());
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        Map<LocalDate, Long> totalClicks = urlMappingServ.getTotalClicksByUserAndDate(user, start, end);
        return ResponseEntity.ok(totalClicks);
    }
}

