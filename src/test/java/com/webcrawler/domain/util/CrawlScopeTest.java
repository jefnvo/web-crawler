package com.webcrawler.domain.util;

import org.junit.jupiter.api.Test;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;

public class CrawlScopeTest {

    @Test
    void shouldAcceptUrlOnSameHost() {
        var scope = new CrawlScope("crawlme.monzo.com");
        assertTrue(scope.isInScope(URI.create("https://crawlme.monzo.com/about")));
    }

    @Test
    void shouldRejectUrlOnDifferentHost() {
         var scope = new CrawlScope("crawlme.monzo.com");
         assertFalse(scope.isInScope(URI.create(("https://monzo.com/about"))));
    }

    @Test
    void shouldRejectSubdomainOfAllowedHost() {
        var scope = new CrawlScope("crawlme.monzo.com");
        assertFalse(scope.isInScope(URI.create("https://community.monzo.com/page")));
    }

    @Test
    void shouldRejectUriWithNoHost() {
        var scope = new CrawlScope("crawlme.monzo.com");
        assertFalse(scope.isInScope(URI.create("/relative/path")));
    }

    @Test
    void shouldRejectMailtoScheme() {
        var scope = new CrawlScope("crawlme.monzo.com");
        assertFalse(scope.isInScope(URI.create("mailto:support@crawlme.monzo.com")));
    }

    @Test
    void shouldRejectFtpScheme() {
        var scope = new CrawlScope("crawlme.monzo.com");
        assertFalse(scope.isInScope(URI.create("ftp://crawlme.monzo.com/file.txt")));
    }

    @Test
    void shouldAcceptHttpScheme() {
        var scope = new CrawlScope("crawlme.monzo.com");
        assertTrue(scope.isInScope(URI.create("http://crawlme.monzo.com/page")));
    }
}
