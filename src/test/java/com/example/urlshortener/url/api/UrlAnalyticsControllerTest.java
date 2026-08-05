package com.example.urlshortener.url.api;

import com.example.urlshortener.common.error.GlobalExceptionHandler;
import com.example.urlshortener.url.api.dto.UrlAnalyticsResponse;
import com.example.urlshortener.url.application.UrlAnalyticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UrlAnalyticsControllerTest {

    private UrlAnalyticsService analyticsService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        analyticsService = mock(UrlAnalyticsService.class);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new UrlAnalyticsController(
                                analyticsService))
                .setControllerAdvice(
                        new GlobalExceptionHandler())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(
                                objectMapper))
                .build();
    }

    @Test
    void returnsAggregateAnalytics()
            throws Exception {
        when(analyticsService.getAnalytics("travel-2026"))
                .thenReturn(new UrlAnalyticsResponse(
                        "travel-2026",
                        URI.create(
                                "https://www.example.com/travel"),
                        Instant.parse(
                                "2026-08-05T04:36:10Z"),
                        4,
                        Instant.parse(
                                "2026-08-05T05:19:49Z")
                ));

        mockMvc.perform(get(
                        "/api/v1/urls/travel-2026/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode")
                        .value("travel-2026"))
                .andExpect(jsonPath("$.clickCount")
                        .value(4))
                .andExpect(jsonPath("$.lastAccessedAt")
                        .value(
                                "2026-08-05T05:19:49Z"));
    }
}
