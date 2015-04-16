package com.adobe.primetime.adde.exception;

/**
 * Created by ramascan on 13/04/15.
 */
public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
