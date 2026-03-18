package com.shortener.url_shortener.service;

import com.shortener.url_shortener.dto.ClickEventDTO;
import com.shortener.url_shortener.dto.UrlMappingDTO;
import com.shortener.url_shortener.models.ClickEvent;
import com.shortener.url_shortener.models.UrlMapping;
import com.shortener.url_shortener.models.User;
import com.shortener.url_shortener.repository.ClickEventRepository;
import com.shortener.url_shortener.repository.UrlMappingRepository;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UrlMappingService {

    private static final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random random = new Random();

    private UrlMappingRepository urlMappingRepo;

    private ClickEventRepository clickEventRepo;

    @Retry(name = "CompanyBreaker")
    public UrlMappingDTO createShortUrl(String originalUrl, User user) {
        String shortUrl = generateShortUrl();
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        UrlMapping savedUrlMapping = urlMappingRepo.save(urlMapping);

        return convertToDto(savedUrlMapping);
    }

    private UrlMappingDTO convertToDto(UrlMapping urlMapping) {
        UrlMappingDTO urlMappingDTO = new UrlMappingDTO();
        urlMappingDTO.setId(urlMapping.getId());
        urlMappingDTO.setOriginalUrl(urlMapping.getOriginalUrl());
        urlMappingDTO.setShortUrl(urlMapping.getShortUrl());
        urlMappingDTO.setClickCount(urlMapping.getClickCount());
        urlMappingDTO.setCreatedDate(urlMapping.getCreatedDate());
        urlMappingDTO.setUsername(urlMapping.getUser().getUsername());

        return urlMappingDTO;

    }

    public String generateShortUrl() {

        String shortUrl;

        do {

            StringBuilder sb = new StringBuilder(7);

            for (int i = 0; i < 7; i++)
                sb.append(characters.charAt(random.nextInt(characters.length())));

            shortUrl = sb.toString();

        }while(urlMappingRepo.existsByShortUrl(shortUrl));

        return shortUrl;
    }

    public List<UrlMappingDTO> getUrlsByUser(User user) {
        return urlMappingRepo.findByUser(user)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<ClickEventDTO> getClickEventsByDate(String shortUrl, LocalDateTime start, LocalDateTime end) {
        UrlMapping urlMapping = urlMappingRepo.findByShortUrl(shortUrl);
        if (urlMapping != null) {
            return clickEventRepo.findByUrlMappingAndClickDateBetween(urlMapping, start, end)
                    .stream()
                    .collect(Collectors.groupingBy(click ->
                                    click.getClickDate().toLocalDate(), Collectors.counting()))
                     .entrySet().stream()
                    .map(entry -> {
                        ClickEventDTO clickEventDTO = new ClickEventDTO();
                        clickEventDTO.setClickDate(entry.getKey());
                        clickEventDTO.setCount(entry.getValue());
                        return clickEventDTO;
                    })
                    .toList();
        }

        return null;

    }

    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end) {
         List<UrlMapping> urlMappings = urlMappingRepo.findByUser(user);
         List<ClickEvent> clickEvents = clickEventRepo.findByUrlMappingInAndClickDateBetween(urlMappings, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
         return clickEvents.stream()
                 .collect(Collectors.groupingBy(click ->
                         click.getClickDate().toLocalDate(), Collectors.counting()));
    }

    @Cacheable(value = "urls", key = "#shortUrl")
    public UrlMapping getOriginalUrl(String shortUrl) {
        UrlMapping urlMapping = urlMappingRepo.findByShortUrl(shortUrl);
       if(urlMapping != null){
           urlMapping.setClickCount(urlMapping.getClickCount()+1);
           urlMappingRepo.save(urlMapping);

        ClickEvent clickEvent = new ClickEvent();
        clickEvent.setClickDate(LocalDateTime.now());
        clickEvent.setUrlMapping(urlMapping);
        clickEventRepo.save(clickEvent);
       }

        return urlMapping;
    }
}

