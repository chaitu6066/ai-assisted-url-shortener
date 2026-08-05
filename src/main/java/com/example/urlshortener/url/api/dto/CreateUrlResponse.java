package com.example.urlshortener.url.api.dto;

import java.net.URI;
import java.time.Instant;

public record CreateUrlResponse(
        String shortCode,
        URI shortUrl,
        URI originalUrl,
        Instant createdAt
) {
}
