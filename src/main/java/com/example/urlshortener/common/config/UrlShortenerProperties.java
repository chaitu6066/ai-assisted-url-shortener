package com.example.urlshortener.common.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.Set;

@Validated
@ConfigurationProperties(prefix = "url-shortener")
public record UrlShortenerProperties(
        @NotNull URI baseUrl,
        @Min(6) @Max(16) int generatedCodeLength,
        @Min(1) @Max(10) int maxAllocationAttempts,
        Set<String> reservedAliases
) {
    public UrlShortenerProperties {
        reservedAliases = reservedAliases == null ? Set.of() : Set.copyOf(reservedAliases);
    }
}
