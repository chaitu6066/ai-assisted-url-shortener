package com.example.urlshortener.url.application;

import com.example.urlshortener.url.api.dto.CreateUrlResponse;
import com.example.urlshortener.url.domain.UrlMapping;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class UrlCreationService {

    private final ShortCodeAllocator allocator;
    private final ShortUrlFactory shortUrlFactory;

    public UrlCreationService(
            ShortCodeAllocator allocator,
            ShortUrlFactory shortUrlFactory
    ) {
        this.allocator = allocator;
        this.shortUrlFactory = shortUrlFactory;
    }

    public CreateUrlResponse create(String originalUrl) {
        String normalizedUrl = originalUrl.trim();
        UrlMapping mapping =
                allocator.allocate(normalizedUrl);

        return new CreateUrlResponse(
                mapping.getShortCode(),
                shortUrlFactory.create(mapping.getShortCode()),
                URI.create(mapping.getOriginalUrl()),
                mapping.getCreatedAt()
        );
    }
}
