package com.webcrawler.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.webcrawler.domain.port.out.LinkExtractor;
import com.webcrawler.domain.port.out.PageFetcher;
import com.webcrawler.domain.util.CrawlScope;
import com.webcrawler.fixtures.HtmlPages;
import com.webcrawler.fixtures.UriFixtures;

class PageProcessorTest {

    private final PageFetcher fetcher = mock(PageFetcher.class);
    private final LinkExtractor extractor = mock(LinkExtractor.class);
    private final PageProcessor processor = new DefaultPageProcessor(fetcher, extractor);
    private final CrawlScope scope = new CrawlScope(UriFixtures.MONZO_ROOT_URI.getHost());

    @Test
    void shouldReturnInScopeNormalizedLinks() {
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(UriFixtures.MONZO_ROOT_URI, HtmlPages.MINIMAL))
            .thenReturn(Set.of(UriFixtures.MONZO_ABOUT_URI));

        var links = processor.fetchLinks(UriFixtures.MONZO_ROOT_URI, scope);

        assertEquals(Set.of(UriFixtures.MONZO_ABOUT_URI), links);
    }

    @Test
    void shouldFilterOutOfScopeLinks() {
        var external = UriFixtures.EXTERNAL_PAGE;
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(UriFixtures.MONZO_ROOT_URI, HtmlPages.MINIMAL))
            .thenReturn(Set.of(UriFixtures.MONZO_ABOUT_URI, external));

        var links = processor.fetchLinks(UriFixtures.MONZO_ROOT_URI, scope);

        assertEquals(Set.of(UriFixtures.MONZO_ABOUT_URI), links);
    }

    @Test
    void shouldReturnEmptySetWhenNoLinks() {
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(UriFixtures.MONZO_ROOT_URI, HtmlPages.MINIMAL)).thenReturn(Set.of());

        var links = processor.fetchLinks(UriFixtures.MONZO_ROOT_URI, scope);

        assertTrue(links.isEmpty());
    }

    @Test
    void shouldNormalizeLinks() {
        var unnormalized = URI.create(UriFixtures.MONZO_ABOUT + "?foo-bar#section");
        var normalized   = URI.create(UriFixtures.MONZO_ABOUT);
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(UriFixtures.MONZO_ROOT_URI, HtmlPages.MINIMAL))
            .thenReturn(Set.of(unnormalized));

        var links = processor.fetchLinks(UriFixtures.MONZO_ROOT_URI, scope);

        assertEquals(Set.of(normalized), links);
    }
}
