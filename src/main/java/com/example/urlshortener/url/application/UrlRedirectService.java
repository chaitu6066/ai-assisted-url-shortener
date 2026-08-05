package com.example.urlshortener.url.application;

import com.example.urlshortener.url.infrastructure.UrlMappingCommandRepository;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class UrlRedirectService {

    private final UrlMappingCommandRepository commandRepository;

    public UrlRedirectService(
            UrlMappingCommandRepository commandRepository
    ) {
        this.commandRepository = commandRepository;
    }

    public URI resolve(String shortCode) {
        return commandRepository
                .recordClickAndGetOriginalUrl(shortCode)
                .map(URI::create)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(shortCode));
    }
}
