package com.example.urlshortener.url.application;

import com.example.urlshortener.common.config.UrlShortenerProperties;
import com.example.urlshortener.url.domain.ShortCodeGenerator;
import com.example.urlshortener.url.domain.UrlMapping;
import com.example.urlshortener.url.infrastructure.UrlMappingCommandRepository;
import org.springframework.stereotype.Service;

@Service
public class ShortCodeAllocator {

    private final ShortCodeGenerator generator;
    private final UrlMappingCommandRepository commandRepository;
    private final UrlShortenerProperties properties;

    public ShortCodeAllocator(
            ShortCodeGenerator generator,
            UrlMappingCommandRepository commandRepository,
            UrlShortenerProperties properties
    ) {
        this.generator = generator;
        this.commandRepository = commandRepository;
        this.properties = properties;
    }

    public UrlMapping allocate(String originalUrl) {
        for (int attempt = 0;
             attempt < properties.maxAllocationAttempts();
             attempt++) {

            String candidate =
                    generator.generate(
                            properties.generatedCodeLength());

            var inserted =
                    commandRepository.insertIfAbsent(
                            candidate,
                            originalUrl
                    );

            if (inserted.isPresent()) {
                return inserted.get();
            }
        }

        throw new ShortCodeAllocationException();
    }
}
