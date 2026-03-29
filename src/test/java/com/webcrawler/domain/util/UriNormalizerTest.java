package com.webcrawler.domain.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;
import org.junit.jupiter.api.Test;

public class UriNormalizerTest {
    @Test
    void shoudlStripFragment() {
        var uri = URI.create("https://crawlme.monzo.com/about#section");
        assertEquals(URI.create("https://crawlme.monzo.com/about"),
                    UriNormalizer.normalize(uri));
    }

    @Test
    void shouldStripTrailingSlash() {
        var uri = URI.create("https://crawlme.monzo.com/about/");
        assertEquals(URI.create("https://crawlme.monzo.com/about"), 
                    UriNormalizer.normalize(uri));
    }

    @Test
    void shouldKeepRootPathWhenNoTrailingSlashToStrip() {
        var uri = URI.create("https://crawlme.monzo.com/");
        assertEquals(
            URI.create("https://crawlme.monzo.com/"),   // ← root slash must be kept
            UriNormalizer.normalize(uri)
        );
    }

    @Test
    void shoudlStripFragmentAndTrailingSlash() {
        var uri = URI.create("https://crawlme.monzo.com/about/#section");
        assertEquals(URI.create("https://crawlme.monzo.com/about"),
                    UriNormalizer.normalize(uri));
    }

    @Test
    void shouldStripeDefaultPort() {
        var uri = URI.create("http://crawlme.monzo.com:80/page");
        assertEquals(URI.create("http://crawlme.monzo.com/page"), 
                    UriNormalizer.normalize(uri));
    }

    @Test
    void shouldLowercaseSchemeAndHost() {
        var uri = URI.create("HTTPS://CrawlMe.Monzo.Com/About");
        assertEquals(URI.create("https://crawlme.monzo.com/About"), 
                    UriNormalizer.normalize(uri));
    }
    
}
