package com.example.urlshortener.url.api.dto;

import com.example.urlshortener.url.domain.ValidHttpUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUrlRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 2048, message = "must not exceed 2048 characters")
        @ValidHttpUrl
        String originalUrl,

        @Pattern(
                regexp = "^$|^[a-z0-9](?:[a-z0-9-]{1,30}[a-z0-9])$",
                message = "must be 3-32 lowercase letters, digits, or hyphens and must start and end with a letter or digit"
        )
        String customAlias
) {
}
