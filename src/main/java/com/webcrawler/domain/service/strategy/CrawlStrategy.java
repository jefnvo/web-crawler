package com.webcrawler.domain.service.strategy;

import java.net.URI;

public interface CrawlStrategy {
    void crawl(URI start);
}
