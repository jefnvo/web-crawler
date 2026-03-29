package com.webcrawler.domain.service;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

import com.webcrawler.domain.port.out.LinkExtractor;
import com.webcrawler.domain.port.out.PageFetcher;
import com.webcrawler.domain.util.CrawlScope;
import com.webcrawler.domain.util.UriNormalizer;

public class DefaultPageProcessor implements PageProcessor {

    private final PageFetcher fetcher;
    private final LinkExtractor extractor;

    public DefaultPageProcessor(PageFetcher fetcher, LinkExtractor extractor) {
        this.fetcher = fetcher;
        this.extractor = extractor;
    }

    @Override
    public Set<URI> fetchLinks(URI uri, CrawlScope scope) {
        var html = fetcher.fetch(uri);
        return extractor.extract(uri, html).stream()
                        .filter(scope::isInScope)
                        .map(UriNormalizer::normalize)
                        .collect(Collectors.toUnmodifiableSet());
    }
}
