package com.example.urlshortener.url.api;

import com.example.urlshortener.url.application.UrlRedirectService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final UrlRedirectService redirectService;

    public RedirectController(UrlRedirectService redirectService) {
        this.redirectService = redirectService;
    }

    @GetMapping("/{shortCode:[A-Za-z0-9][A-Za-z0-9-]{1,30}[A-Za-z0-9]}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode
    ) {
        URI destination = redirectService.resolve(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(destination);
        headers.setCacheControl(CacheControl.noStore());

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
