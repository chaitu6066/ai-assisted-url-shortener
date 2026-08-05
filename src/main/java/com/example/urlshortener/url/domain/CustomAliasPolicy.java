package com.example.urlshortener.url.domain;

import com.example.urlshortener.common.config.UrlShortenerProperties;
import com.example.urlshortener.url.application.ReservedCustomAliasException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomAliasPolicy {

    private final Set<String> reservedAliases;

    public CustomAliasPolicy(UrlShortenerProperties properties) {
        this.reservedAliases = properties.reservedAliases()
                .stream()
                .map(alias -> alias.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public Optional<String> normalizeAndValidate(String customAlias) {
        if (customAlias == null || customAlias.isBlank()) {
            return Optional.empty();
        }

        String normalized = customAlias.trim();

        if (reservedAliases.contains(
                normalized.toLowerCase(Locale.ROOT))) {
            throw new ReservedCustomAliasException(normalized);
        }

        return Optional.of(normalized);
    }
}
