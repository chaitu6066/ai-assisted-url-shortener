package com.example.urlshortener.url.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecureRandomBase62ShortCodeGeneratorTest {

    private final SecureRandomBase62ShortCodeGenerator generator =
            new SecureRandomBase62ShortCodeGenerator();

    @Test
    void generatesRequestedLengthUsingBase62Alphabet() {
        String code = generator.generate(10);

        assertThat(code)
                .hasSize(10)
                .matches("[A-Za-z0-9]{10}");
    }
}
