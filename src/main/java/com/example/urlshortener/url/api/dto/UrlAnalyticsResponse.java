package com.example.urlshortener.url.api.dto;

import java.net.URI;
import java.time.Instant;

public record UrlAnalyticsResponse(
        String shortCode,
        URI originalUrl,
        Instant createdAt,
        long clickCount,
        Instant lastAccessedAt
) {
}
