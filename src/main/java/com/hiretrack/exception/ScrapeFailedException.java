package com.HireTrack.exception;

public class ScrapeFailedException extends RuntimeException {
    public ScrapeFailedException(String message) {
        super(message);
    }
}