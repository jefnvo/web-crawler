package com.webcrawler.domain.service;

import java.net.URI;
import java.util.Set;

import com.webcrawler.domain.util.CrawlScope;

public interface PageProcessor {
    Set<URI> fetchLinks(URI uri, CrawlScope scope);
}
