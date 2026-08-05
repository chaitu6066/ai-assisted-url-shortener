package com.example.urlshortener.url.domain;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureRandomBase62ShortCodeGenerator
        implements ShortCodeGenerator {

    static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate(int length) {
        StringBuilder code = new StringBuilder(length);

        for (int index = 0; index < length; index++) {
            int alphabetIndex =
                    secureRandom.nextInt(ALPHABET.length());
            code.append(ALPHABET.charAt(alphabetIndex));
        }

        return code.toString();
    }
}
