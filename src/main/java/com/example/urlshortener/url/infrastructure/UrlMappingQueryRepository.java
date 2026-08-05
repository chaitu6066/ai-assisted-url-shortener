package com.example.urlshortener.url.infrastructure;

import com.example.urlshortener.url.domain.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlMappingQueryRepository
        extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortCode(String shortCode);
}
