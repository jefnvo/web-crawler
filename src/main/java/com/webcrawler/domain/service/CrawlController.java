package com.webcrawler.domain.service;

import java.net.URI;

import com.webcrawler.domain.port.in.CrawlUseCase;
import com.webcrawler.domain.service.strategy.CrawlStrategy;
import com.webcrawler.domain.util.CrawlScope;

public class CrawlController implements CrawlUseCase {

    private final CrawlStrategy strategy;

    public CrawlController(CrawlStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void crawl(String url) {
        URI uri = parseAndValidate(url);
        strategy.crawl(uri);
    }

    private URI parseAndValidate(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or blank");
        }

        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Malformed URL: " + url, e);
        }

        if (!CrawlScope.isCrawlableScheme(uri)) {
            throw new IllegalArgumentException(
                "URL scheme must be http or https, got: " + uri.getScheme());
        }
        return uri;
    }
}
