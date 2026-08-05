package com.example.urlshortener.url.api;

import com.example.urlshortener.url.api.dto.UrlAnalyticsResponse;
import com.example.urlshortener.url.application.UrlAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlAnalyticsController {

    private final UrlAnalyticsService analyticsService;

    public UrlAnalyticsController(
            UrlAnalyticsService analyticsService
    ) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{shortCode}/analytics")
    public ResponseEntity<UrlAnalyticsResponse> getAnalytics(
            @PathVariable String shortCode
    ) {
        return ResponseEntity.ok(
                analyticsService.getAnalytics(shortCode)
        );
    }
}
