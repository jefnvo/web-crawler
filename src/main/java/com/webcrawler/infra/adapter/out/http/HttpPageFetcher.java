package com.webcrawler.infra.adapter.out.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.webcrawler.domain.port.out.PageFetcher;
import com.webcrawler.infra.config.HttpClientConfig;
import java.util.Set;
import com.webcrawler.domain.port.out.PageFetchException;

public class HttpPageFetcher implements PageFetcher {
       
    private static final int HTTP_STATUS_OK = 200;
    private static final Set<Integer> RETRYABLE_STATUSES = Set.of(408, 429, 502, 503, 504);
    private final HttpClient client;
    private final HttpClientConfig config;

    public HttpPageFetcher(HttpClientConfig config) {
        this.config = config;
        this.client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(config.connectTimeout())
                    .build();
        }

    @Override
    public String fetch(URI uri) {
        var lastException = (IOException) null;

        for (var attempt = 0; attempt <= config.maxRetries(); attempt++) {
            try {
                var request = HttpRequest.newBuilder(uri)
                        .GET()
                        .timeout(config.requestTimeout())
                        .header("User-Agent", config.userAgent())
                        .build();

                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == HTTP_STATUS_OK) {
                    return response.body();
                }

                if (!RETRYABLE_STATUSES.contains(response.statusCode())) {
                    throw new PageFetchException("Non-retryable HTTP status %d for: %s"
                            .formatted(response.statusCode(), uri));
                }

            } catch (IOException e) {
                lastException = e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PageFetchException("Fetch interrupted for: %s".formatted(uri), e);
            }

            if (attempt < config.maxRetries()) {
                backoff(attempt);    // ← only sleep if there's a next attempt
            }
        }

        throw new PageFetchException(
                "Failed to fetch %s after %d attempts".formatted(uri, config.maxRetries() + 1),
                lastException);
    }


    private void backoff(int attempt) {
        try {
            Thread.sleep((long) Math.pow(2, attempt) * config.initialBackoffMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
