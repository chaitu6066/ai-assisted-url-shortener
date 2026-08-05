package com.example.urlshortener.url.application;

public class ShortUrlNotFoundException extends RuntimeException {

    public ShortUrlNotFoundException(String shortCode) {
        super("No URL mapping exists for short code '"
                + shortCode
                + "'.");
    }
}
