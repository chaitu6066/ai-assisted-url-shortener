package com.example.urlshortener.url.api.dto;

import com.example.urlshortener.url.domain.ValidHttpUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUrlRequest(
        @NotBlank(message = "must not be blank")
        @Size(max = 2048, message = "must not exceed 2048 characters")
        @ValidHttpUrl
        String originalUrl
) {
}
