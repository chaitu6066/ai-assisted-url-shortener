package com.example.urlshortener.url.application;

import com.example.urlshortener.url.domain.UrlMapping;
import com.example.urlshortener.url.infrastructure.UrlMappingQueryRepository;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UrlAnalyticsServiceTest {

    private final UrlMappingQueryRepository repository =
            mock(UrlMappingQueryRepository.class);

    private final UrlAnalyticsService service =
            new UrlAnalyticsService(repository);

    @Test
    void returnsAggregateAnalyticsWithoutPersonalData() {
        Instant createdAt =
                Instant.parse("2026-08-05T04:00:00Z");
        Instant lastAccessedAt =
                Instant.parse("2026-08-05T05:00:00Z");

        UrlMapping mapping = new UrlMapping(
                1L,
                "travel-2026",
                "https://www.example.com/travel",
                createdAt,
                lastAccessedAt,
                3
        );

        when(repository.findByShortCode("travel-2026"))
                .thenReturn(Optional.of(mapping));

        var response =
                service.getAnalytics("travel-2026");

        assertThat(response.shortCode())
                .isEqualTo("travel-2026");
        assertThat(response.originalUrl())
                .isEqualTo(URI.create(
                        "https://www.example.com/travel"));
        assertThat(response.createdAt())
                .isEqualTo(createdAt);
        assertThat(response.clickCount())
                .isEqualTo(3);
        assertThat(response.lastAccessedAt())
                .isEqualTo(lastAccessedAt);
    }

    @Test
    void rejectsUnknownShortCode() {
        when(repository.findByShortCode("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getAnalytics("missing"))
                .isInstanceOf(ShortUrlNotFoundException.class);
    }
}
