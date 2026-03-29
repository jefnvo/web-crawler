package com.webcrawler.domain.util;

import org.junit.jupiter.api.Test;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;

import com.webcrawler.fixtures.UriFixtures;

public class CrawlScopeTest {

    private static final CrawlScope SCOPE = new CrawlScope(UriFixtures.MONZO_ROOT_URI.getHost());

    @Test
    void shouldAcceptUrlOnSameHost() {
        assertTrue(SCOPE.isInScope(URI.create("https://crawlme.monzo.com/about")));
    }

    @Test
    void shouldRejectUrlOnDifferentHost() {
        assertFalse(SCOPE.isInScope(URI.create("https://monzo.com/about")));
    }

    @Test
    void shouldRejectSubdomainOfAllowedHost() {
        assertFalse(SCOPE.isInScope(URI.create("https://community.monzo.com/page")));
    }

    @Test
    void shouldRejectUriWithNoHost() {
        assertFalse(SCOPE.isInScope(URI.create("/relative/path")));
    }

    @Test
    void shouldRejectMailtoScheme() {
        assertFalse(SCOPE.isInScope(URI.create("mailto:support@crawlme.monzo.com")));
    }

    @Test
    void shouldRejectFtpScheme() {
        assertFalse(SCOPE.isInScope(URI.create("ftp://crawlme.monzo.com/file.txt")));
    }

    @Test
    void shouldAcceptHttpScheme() {
        assertTrue(SCOPE.isInScope(URI.create("http://crawlme.monzo.com/page")));
    }
}
