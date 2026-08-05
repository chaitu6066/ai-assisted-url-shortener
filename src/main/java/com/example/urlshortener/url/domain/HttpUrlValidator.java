package com.example.urlshortener.url.domain;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

public class HttpUrlValidator
        implements ConstraintValidator<ValidHttpUrl, String> {

    private static final Set<String> ALLOWED_SCHEMES =
            Set.of("http", "https");

    @Override
    public boolean isValid(
            String value,
            ConstraintValidatorContext context
    ) {
        if (value == null || value.isBlank()) {
            return true;
        }

        if (containsControlCharacter(value)) {
            return false;
        }

        try {
            URI uri = new URI(value.trim());
            String scheme = uri.getScheme();

            return uri.isAbsolute()
                    && scheme != null
                    && ALLOWED_SCHEMES.contains(
                            scheme.toLowerCase(Locale.ROOT))
                    && uri.getHost() != null
                    && !uri.getHost().isBlank()
                    && uri.getRawUserInfo() == null;
        } catch (URISyntaxException exception) {
            return false;
        }
    }

    private boolean containsControlCharacter(String value) {
        return value.chars()
                .anyMatch(character ->
                        character < 32 || character == 127);
    }
}
