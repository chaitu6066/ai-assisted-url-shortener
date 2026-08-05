package com.example.urlshortener.url.application;

import com.example.urlshortener.common.config.UrlShortenerProperties;
import com.example.urlshortener.url.domain.ShortCodeGenerator;
import com.example.urlshortener.url.domain.UrlMapping;
import com.example.urlshortener.url.infrastructure.UrlMappingCommandRepository;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShortCodeAllocatorTest {

    private final ShortCodeGenerator generator =
            mock(ShortCodeGenerator.class);

    private final UrlMappingCommandRepository repository =
            mock(UrlMappingCommandRepository.class);

    private final UrlShortenerProperties properties =
            new UrlShortenerProperties(
                    URI.create("http://localhost:8081"),
                    10,
                    2,
                    Set.of()
            );

    private final ShortCodeAllocator allocator =
            new ShortCodeAllocator(
                    generator,
                    repository,
                    properties
            );

    @Test
    void retriesAfterCollisionAndReturnsSecondMapping() {
        UrlMapping expected = mapping("BBBBBBBBBB");

        when(generator.generate(10))
                .thenReturn(
                        "AAAAAAAAAA",
                        "BBBBBBBBBB"
                );

        when(repository.insertIfAbsent(
                "AAAAAAAAAA",
                "https://example.com"
        )).thenReturn(Optional.empty());

        when(repository.insertIfAbsent(
                "BBBBBBBBBB",
                "https://example.com"
        )).thenReturn(Optional.of(expected));

        UrlMapping actual =
                allocator.allocate("https://example.com");

        assertThat(actual.getShortCode())
                .isEqualTo("BBBBBBBBBB");

        verify(repository).insertIfAbsent(
                "AAAAAAAAAA",
                "https://example.com"
        );

        verify(repository).insertIfAbsent(
                "BBBBBBBBBB",
                "https://example.com"
        );
    }

    @Test
    void failsAfterConfiguredAttemptsAreExhausted() {
        when(generator.generate(10))
                .thenReturn(
                        "AAAAAAAAAA",
                        "BBBBBBBBBB"
                );

        when(repository.insertIfAbsent(
                "AAAAAAAAAA",
                "https://example.com"
        )).thenReturn(Optional.empty());

        when(repository.insertIfAbsent(
                "BBBBBBBBBB",
                "https://example.com"
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                allocator.allocate("https://example.com"))
                .isInstanceOf(
                        ShortCodeAllocationException.class);
    }

    private UrlMapping mapping(String shortCode) {
        return new UrlMapping(
                1L,
                shortCode,
                "https://example.com",
                Instant.parse("2026-08-05T00:00:00Z"),
                null,
                0
        );
    }
}
