package com.example.urlshortener.url.application;

public class CustomAliasAlreadyExistsException extends RuntimeException {

    public CustomAliasAlreadyExistsException(String customAlias) {
        super("The requested custom alias '" + customAlias + "' is already in use.");
    }
}
