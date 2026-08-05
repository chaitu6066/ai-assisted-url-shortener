package com.example.urlshortener.url.api;

import com.example.urlshortener.common.error.GlobalExceptionHandler;
import com.example.urlshortener.url.api.dto.CreateUrlResponse;
import com.example.urlshortener.url.application.CustomAliasAlreadyExistsException;
import com.example.urlshortener.url.application.UrlCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UrlManagementControllerTest {

    private UrlCreationService creationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        creationService = mock(UrlCreationService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new UrlManagementController(creationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createsCustomAliasAndReturnsLocationHeader()
            throws Exception {
        Instant createdAt =
                Instant.parse("2026-08-05T04:36:10Z");

        when(creationService.create(
                "https://www.example.com/travel",
                "travel-2026"
        )).thenReturn(new CreateUrlResponse(
                "travel-2026",
                URI.create(
                        "http://localhost:8081/travel-2026"),
                URI.create(
                        "https://www.example.com/travel"),
                createdAt
        ));

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalUrl":
                                    "https://www.example.com/travel",
                                  "customAlias": "travel-2026"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost:8081/travel-2026"))
                .andExpect(jsonPath("$.shortCode")
                        .value("travel-2026"))
                .andExpect(jsonPath("$.originalUrl")
                        .value(
                                "https://www.example.com/travel"));
    }

    @Test
    void returnsConflictForDuplicateCustomAlias()
            throws Exception {
        when(creationService.create(
                anyString(),
                eq("travel-2026")
        )).thenThrow(
                new CustomAliasAlreadyExistsException(
                        "travel-2026"));

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalUrl":
                                    "https://www.example.com/travel",
                                  "customAlias": "travel-2026"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value(
                                "CUSTOM_ALIAS_ALREADY_EXISTS"));
    }

    @Test
    void returnsStructuredErrorForMalformedJson()
            throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_FAILED"));
    }
}
