package com.example.urlshortener.url.application;

import com.example.urlshortener.url.api.dto.CreateUrlResponse;
import com.example.urlshortener.url.domain.CustomAliasPolicy;
import com.example.urlshortener.url.domain.UrlMapping;
import com.example.urlshortener.url.infrastructure.UrlMappingCommandRepository;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class UrlCreationService {

    private final ShortCodeAllocator allocator;
    private final CustomAliasPolicy customAliasPolicy;
    private final UrlMappingCommandRepository commandRepository;
    private final ShortUrlFactory shortUrlFactory;

    public UrlCreationService(
            ShortCodeAllocator allocator,
            CustomAliasPolicy customAliasPolicy,
            UrlMappingCommandRepository commandRepository,
            ShortUrlFactory shortUrlFactory
    ) {
        this.allocator = allocator;
        this.customAliasPolicy = customAliasPolicy;
        this.commandRepository = commandRepository;
        this.shortUrlFactory = shortUrlFactory;
    }

    public CreateUrlResponse create(
            String originalUrl,
            String customAlias
    ) {
        String normalizedUrl = originalUrl.trim();

        UrlMapping mapping = customAliasPolicy
                .normalizeAndValidate(customAlias)
                .map(alias -> createWithCustomAlias(alias, normalizedUrl))
                .orElseGet(() -> allocator.allocate(normalizedUrl));

        return new CreateUrlResponse(
                mapping.getShortCode(),
                shortUrlFactory.create(mapping.getShortCode()),
                URI.create(mapping.getOriginalUrl()),
                mapping.getCreatedAt()
        );
    }

    private UrlMapping createWithCustomAlias(
            String customAlias,
            String originalUrl
    ) {
        return commandRepository
                .insertIfAbsent(customAlias, originalUrl)
                .orElseThrow(() ->
                        new CustomAliasAlreadyExistsException(customAlias));
    }
}
