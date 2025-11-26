package com.sensor.exception;

/**
 * Exception thrown when an upload ID is not found
 */
public class UploadNotFoundException extends RuntimeException {

    public UploadNotFoundException(String message) {
        super(message);
    }

    public UploadNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}