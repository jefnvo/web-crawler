package com.webcrawler.domain.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import com.webcrawler.domain.service.strategy.CrawlStrategy;
import com.webcrawler.fixtures.UriFixtures;

public class CrawlServiceTest {
    private final CrawlStrategy strategy = mock(CrawlStrategy.class);
    private final CrawlController service = new CrawlController(strategy);

    @Test
    void shoulRejectNullUrl() {
        assertThrows(IllegalArgumentException.class, () -> service.crawl(null));
    }

    @Test
    void shouldRejectBlankUrl() {
        assertThrows(IllegalArgumentException.class, () -> service.crawl("   "));
    }

    @Test
    void shouldRejectMalformedUrl() {
        assertThrows(IllegalArgumentException.class, () -> service.crawl("not a url"));
    }

    @Test
    void shouldRejectNonHttpScheme() {
        assertThrows(IllegalArgumentException.class, () -> service.crawl("ftp://example.com"));
    }

    @Test
    void shouldDelegateValidUrlToStrategy() {
        service.crawl(UriFixtures.MONZO);
        verify(strategy).crawl(UriFixtures.MONZO_ROOT_URI);
    }
}
