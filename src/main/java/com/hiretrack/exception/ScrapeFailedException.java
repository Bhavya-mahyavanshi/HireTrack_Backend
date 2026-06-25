package com.hiretrack.exception;

public class ScrapeFailedException extends RuntimeException {
    public ScrapeFailedException(String message) {
        super(message);
    }
}