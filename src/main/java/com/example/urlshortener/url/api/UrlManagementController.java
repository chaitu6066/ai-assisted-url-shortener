package com.example.urlshortener.url.api;

import com.example.urlshortener.url.api.dto.CreateUrlRequest;
import com.example.urlshortener.url.api.dto.CreateUrlResponse;
import com.example.urlshortener.url.application.UrlCreationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlManagementController {

    private final UrlCreationService creationService;

    public UrlManagementController(UrlCreationService creationService) {
        this.creationService = creationService;
    }

    @PostMapping
    public ResponseEntity<CreateUrlResponse> createUrl(
            @Valid @RequestBody CreateUrlRequest request
    ) {
        CreateUrlResponse response =
                creationService.create(request.originalUrl());

        return ResponseEntity
                .created(response.shortUrl())
                .body(response);
    }
}
