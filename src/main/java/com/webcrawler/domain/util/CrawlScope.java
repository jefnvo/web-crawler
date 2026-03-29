package com.webcrawler.domain.util;

import java.net.URI;
import java.util.Set;

public record CrawlScope(String allowedHost) {

    private static final Set<String> CRAWLABLE_SCHEMES = Set.of("http", "https");

    public static boolean isCrawlableScheme(URI uri) {
        String scheme = uri.getScheme();
        return scheme != null && CRAWLABLE_SCHEMES.contains(scheme);
    }
    
    public boolean isInScope(URI uri) {
        return isCrawlableScheme(uri) && allowedHost.equals(uri.getHost());
    }
}
