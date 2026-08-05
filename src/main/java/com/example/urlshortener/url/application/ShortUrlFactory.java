package com.example.urlshortener.url.application;

import com.example.urlshortener.common.config.UrlShortenerProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class ShortUrlFactory {

    private final String normalizedBaseUrl;

    public ShortUrlFactory(
            UrlShortenerProperties properties
    ) {
        this.normalizedBaseUrl =
                removeTrailingSlashes(
                        properties.baseUrl().toString());
    }

    public URI create(String shortCode) {
        return URI.create(
                normalizedBaseUrl + "/" + shortCode);
    }

    private String removeTrailingSlashes(String value) {
        int end = value.length();

        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }

        return value.substring(0, end);
    }
}
