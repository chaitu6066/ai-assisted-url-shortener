package com.example.urlshortener.url.application;

import com.example.urlshortener.url.infrastructure.UrlMappingQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Service
public class UrlRedirectService {

    private final UrlMappingQueryRepository queryRepository;

    public UrlRedirectService(
            UrlMappingQueryRepository queryRepository
    ) {
        this.queryRepository = queryRepository;
    }

    @Transactional(readOnly = true)
    public URI resolve(String shortCode) {
        return queryRepository.findByShortCode(shortCode)
                .map(mapping ->
                        URI.create(mapping.getOriginalUrl()))
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(shortCode));
    }
}
