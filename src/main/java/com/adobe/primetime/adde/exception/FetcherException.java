package com.adobe.primetime.adde.exception;

/**
 * Created by ramascan on 13/04/15.
 */
public class FetcherException extends RuntimeException {
    public FetcherException(String message) {
        super(message);
    }
    public FetcherException(String message, Throwable cause) {
        super(message, cause);
    }
}
