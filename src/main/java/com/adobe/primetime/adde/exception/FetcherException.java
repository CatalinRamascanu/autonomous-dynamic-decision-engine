package com.adobe.primetime.adde.exception;

public class FetcherException extends RuntimeException {
    public FetcherException(String message) {
        super(message);
    }
    public FetcherException(String message, Throwable cause) {
        super(message, cause);
    }
}
