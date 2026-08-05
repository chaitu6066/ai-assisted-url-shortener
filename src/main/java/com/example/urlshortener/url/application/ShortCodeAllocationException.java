package com.example.urlshortener.url.application;

public class ShortCodeAllocationException extends RuntimeException {

    public ShortCodeAllocationException() {
        super("A short code could not be allocated. Please retry.");
    }
}
