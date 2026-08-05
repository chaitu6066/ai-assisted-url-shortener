package com.example.urlshortener.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    OpenAPI urlShortenerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("URL Shortener API")
                .version("v1")
                .description("AI-assisted production-oriented URL shortener prototype"));
    }
}
