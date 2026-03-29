package com.webcrawler.infra.config;

import java.time.Duration;

/**
 * Configuration for the HTTP client.
 *
 * <p>In production, these values should be externally injected via environment
 * variables or a config file and mapped here at startup.
 * Hardcoded defaults are provided for local development only and interview purpose.
 */
public record HttpClientConfig(
        Duration connectTimeout,
        Duration requestTimeout,
        String userAgent,
        int maxRetries,
        long initialBackoffMillis
){
    public static HttpClientConfig defaults() {
        return new HttpClientConfig(
                Duration.ofSeconds(10),
                Duration.ofSeconds(30),
                "MonzoWebCrawler/1.0",
                3,
                100L
        );
    }
}
