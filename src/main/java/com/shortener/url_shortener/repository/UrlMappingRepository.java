package com.shortener.url_shortener.repository;

import com.shortener.url_shortener.models.UrlMapping;
import com.shortener.url_shortener.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    UrlMapping findByShortUrl(String shortUrl);
    List<UrlMapping> findByUser(User user);
    boolean existsByShortUrl(String shortUrl);
}
