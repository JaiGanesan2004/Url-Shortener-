package com.shortener.url_shortener.controllers;

import com.shortener.url_shortener.models.UrlMapping;
import com.shortener.url_shortener.service.UrlMappingService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class RedirectController {

    private UrlMappingService urlMappingServ;

    @RateLimiter(name = "CompanyBreaker")
    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl){
        UrlMapping urlMapping = urlMappingServ.getOriginalUrl(shortUrl);
        if(urlMapping != null){
            urlMappingServ.recordClick(urlMapping);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Location", urlMapping.getOriginalUrl());
            return ResponseEntity.status(302).headers(httpHeaders).build();
        }

        return ResponseEntity.notFound().build();
    }
}
