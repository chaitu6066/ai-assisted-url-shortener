package com.example.urlshortener.url.application;

import com.example.urlshortener.url.infrastructure.UrlMappingCommandRepository;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UrlRedirectServiceTest {

    private final UrlMappingCommandRepository repository =
            mock(UrlMappingCommandRepository.class);

    private final UrlRedirectService service =
            new UrlRedirectService(repository);

    @Test
    void atomicallyRecordsClickAndReturnsDestination() {
        when(repository.recordClickAndGetOriginalUrl("travel-2026"))
                .thenReturn(Optional.of(
                        "https://www.example.com/travel"));

        URI result = service.resolve("travel-2026");

        assertThat(result).isEqualTo(
                URI.create("https://www.example.com/travel"));

        verify(repository)
                .recordClickAndGetOriginalUrl("travel-2026");
    }

    @Test
    void rejectsUnknownShortCode() {
        when(repository.recordClickAndGetOriginalUrl("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolve("missing"))
                .isInstanceOf(ShortUrlNotFoundException.class);
    }
}
