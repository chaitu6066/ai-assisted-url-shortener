package com.example.urlshortener.url.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpUrlValidatorTest {

    private final HttpUrlValidator validator =
            new HttpUrlValidator();

    @Test
    void acceptsAbsoluteHttpAndHttpsUrls() {
        assertThat(validator.isValid(
                "https://example.com/path?id=42",
                null
        )).isTrue();

        assertThat(validator.isValid(
                "http://localhost:8081/test",
                null
        )).isTrue();
    }

    @Test
    void rejectsRelativeAndUnsupportedUrls() {
        assertThat(validator.isValid(
                "example.com",
                null
        )).isFalse();

        assertThat(validator.isValid(
                "javascript:alert(1)",
                null
        )).isFalse();

        assertThat(validator.isValid(
                "file:///etc/passwd",
                null
        )).isFalse();
    }

    @Test
    void rejectsEmbeddedCredentials() {
        assertThat(validator.isValid(
                "https://user:password@example.com",
                null
        )).isFalse();
    }
}
