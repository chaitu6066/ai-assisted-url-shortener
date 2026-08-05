package com.example.urlshortener.url.application;

import com.example.urlshortener.common.config.UrlShortenerProperties;
import com.example.urlshortener.url.domain.CustomAliasPolicy;
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

class UrlCreationServiceTest {

    private final ShortCodeAllocator allocator =
            mock(ShortCodeAllocator.class);

    private final UrlMappingCommandRepository repository =
            mock(UrlMappingCommandRepository.class);

    private final UrlShortenerProperties properties =
            new UrlShortenerProperties(
                    URI.create("http://localhost:8081"),
                    10,
                    5,
                    Set.of("api")
            );

    private final UrlCreationService service =
            new UrlCreationService(
                    allocator,
                    new CustomAliasPolicy(properties),
                    repository,
                    new ShortUrlFactory(properties)
            );

    @Test
    void usesRequestedCustomAliasWhenAvailable() {
        UrlMapping mapping = mapping("travel-2026");

        when(repository.insertIfAbsent(
                "travel-2026",
                "https://example.com"
        )).thenReturn(Optional.of(mapping));

        var response = service.create(
                "https://example.com",
                "travel-2026"
        );

        assertThat(response.shortCode())
                .isEqualTo("travel-2026");

        assertThat(response.shortUrl())
                .isEqualTo(
                        URI.create(
                                "http://localhost:8081/travel-2026"));

        verify(repository).insertIfAbsent(
                "travel-2026",
                "https://example.com"
        );
    }

    @Test
    void rejectsDuplicateCustomAlias() {
        when(repository.insertIfAbsent(
                "travel-2026",
                "https://example.com"
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.create(
                        "https://example.com",
                        "travel-2026"
                ))
                .isInstanceOf(
                        CustomAliasAlreadyExistsException.class);
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
