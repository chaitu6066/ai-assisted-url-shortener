package com.example.urlshortener.url.api;

import com.example.urlshortener.common.error.GlobalExceptionHandler;
import com.example.urlshortener.url.application.ShortUrlNotFoundException;
import com.example.urlshortener.url.application.UrlRedirectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RedirectControllerTest {

    private UrlRedirectService redirectService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        redirectService = mock(UrlRedirectService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new RedirectController(redirectService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void redirectsWithNoStoreCaching()
            throws Exception {
        when(redirectService.resolve("travel-2026"))
                .thenReturn(URI.create(
                        "https://www.example.com/travel"));

        mockMvc.perform(get("/travel-2026"))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        "Location",
                        "https://www.example.com/travel"))
                .andExpect(header().string(
                        "Cache-Control",
                        "no-store"));
    }

    @Test
    void returnsNotFoundForUnknownCode()
            throws Exception {
        when(redirectService.resolve("missing-code"))
                .thenThrow(
                        new ShortUrlNotFoundException(
                                "missing-code"));

        mockMvc.perform(get("/missing-code"))
                .andExpect(status().isNotFound());
    }
}
