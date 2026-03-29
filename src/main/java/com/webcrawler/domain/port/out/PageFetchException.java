package com.webcrawler.domain.port.out;

public class PageFetchException extends RuntimeException {

    public PageFetchException(String message) {
        super(message);
    }

    public PageFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
