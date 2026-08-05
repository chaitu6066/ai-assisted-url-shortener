package com.example.urlshortener.url.application;

public class ReservedCustomAliasException extends RuntimeException {

    public ReservedCustomAliasException(String customAlias) {
        super("The requested custom alias '" + customAlias + "' is reserved.");
    }
}
