package com.webcrawler.infra.config;

import java.time.Duration;

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
