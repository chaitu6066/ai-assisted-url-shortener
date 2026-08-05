package com.example.urlshortener.url.application;

import com.example.urlshortener.url.api.dto.UrlAnalyticsResponse;
import com.example.urlshortener.url.domain.UrlMapping;
import com.example.urlshortener.url.infrastructure.UrlMappingQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Service
public class UrlAnalyticsService {

    private final UrlMappingQueryRepository queryRepository;

    public UrlAnalyticsService(
            UrlMappingQueryRepository queryRepository
    ) {
        this.queryRepository = queryRepository;
    }

    @Transactional(readOnly = true)
    public UrlAnalyticsResponse getAnalytics(
            String shortCode
    ) {
        UrlMapping mapping = queryRepository
                .findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(shortCode));

        return new UrlAnalyticsResponse(
                mapping.getShortCode(),
                URI.create(mapping.getOriginalUrl()),
                mapping.getCreatedAt(),
                mapping.getClickCount(),
                mapping.getLastAccessedAt()
        );
    }
}
