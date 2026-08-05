package com.example.urlshortener.url.domain;

import com.example.urlshortener.common.config.UrlShortenerProperties;
import com.example.urlshortener.url.application.ReservedCustomAliasException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomAliasPolicyTest {

    private final CustomAliasPolicy policy =
            new CustomAliasPolicy(
                    new UrlShortenerProperties(
                            URI.create("http://localhost:8081"),
                            10,
                            5,
                            Set.of("api", "actuator")
                    )
            );

    @Test
    void returnsEmptyWhenAliasIsAbsentOrBlank() {
        assertThat(policy.normalizeAndValidate(null))
                .isEmpty();

        assertThat(policy.normalizeAndValidate("   "))
                .isEmpty();
    }

    @Test
    void acceptsNonReservedAlias() {
        assertThat(policy.normalizeAndValidate("travel-2026"))
                .contains("travel-2026");
    }

    @Test
    void rejectsReservedAliasCaseInsensitively() {
        assertThatThrownBy(() ->
                policy.normalizeAndValidate("api"))
                .isInstanceOf(
                        ReservedCustomAliasException.class);
    }
}
