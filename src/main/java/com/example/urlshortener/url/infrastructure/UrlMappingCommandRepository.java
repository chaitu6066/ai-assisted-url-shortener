package com.example.urlshortener.url.infrastructure;

import com.example.urlshortener.url.domain.UrlMapping;

import java.util.Optional;

public interface UrlMappingCommandRepository {

    Optional<UrlMapping> insertIfAbsent(
            String shortCode,
            String originalUrl
    );

    Optional<String> recordClickAndGetOriginalUrl(
            String shortCode
    );
}
