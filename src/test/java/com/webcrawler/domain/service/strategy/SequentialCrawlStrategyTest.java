package com.webcrawler.domain.service.strategy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.webcrawler.domain.port.out.LinkExtractor;
import com.webcrawler.domain.port.out.PageFetchException;
import com.webcrawler.domain.port.out.PageFetcher;
import com.webcrawler.domain.port.out.ResultReporter;
import com.webcrawler.domain.service.frontier.BfsFrontier;
import com.webcrawler.fixtures.HtmlPages;
import com.webcrawler.fixtures.UriFixtures;

public class SequentialCrawlStrategyTest {

    private final PageFetcher fetcher     = mock(PageFetcher.class);
    private final LinkExtractor extractor = mock(LinkExtractor.class);
    private final ResultReporter reporter = mock(ResultReporter.class);

    private final CrawlStrategy strategy =
            new SequentialCrawlStrategy(fetcher, extractor, reporter, new BfsFrontier());

    @Test
    void shouldVisitOnceSinglePage() {
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(UriFixtures.MONZO_ROOT_URI, HtmlPages.MINIMAL)).thenReturn(Set.of());

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(fetcher, times(1)).fetch(UriFixtures.MONZO_ROOT_URI);
        verify(reporter, times(1)).report(eq(UriFixtures.MONZO_ROOT_URI), eq(Set.of()), anyInt());
    }

    @Test
    void shouldFollowInScopeLink() {
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ABOUT_URI));
        when(fetcher.fetch(UriFixtures.MONZO_ABOUT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ABOUT_URI), any())).thenReturn(Set.of());

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(fetcher, times(1)).fetch(UriFixtures.MONZO_ROOT_URI);
    }

    @Test
    void shouldNotFollowOutOfScopeLinks() {
        var external = URI.create("https://facebook.com/");
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of(external));

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(fetcher, never()).fetch(external);
    }

    @Test
    void shouldNotFollowNonHttpLinks() {
        var mailto = URI.create("mailto:support@example.com");
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of(mailto));

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(fetcher, never()).fetch(mailto);
    }

    @Test
    void shouldNotRevisitAlreadyVisitedPages() {
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ABOUT_URI));
        when(fetcher.fetch(UriFixtures.MONZO_ABOUT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ABOUT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ROOT_URI));

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(fetcher, times(1)).fetch(UriFixtures.MONZO_ROOT_URI);
        verify(fetcher, times(1)).fetch(UriFixtures.MONZO_ABOUT_URI);
    }

    @Test
    void shouldContinueWhenPageFetchFails() {
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ABOUT_URI));
        when(fetcher.fetch(UriFixtures.MONZO_ABOUT_URI)).thenThrow(new PageFetchException("timeout"));

        assertDoesNotThrow(() -> strategy.crawl(UriFixtures.MONZO_ROOT_URI));
        verify(fetcher, times(1)).fetch(UriFixtures.MONZO_ABOUT_URI);
    }

    @Test
    void shouldReportOnlyNewlyDiscoveredLinks() {
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ABOUT_URI));
        when(fetcher.fetch(UriFixtures.MONZO_ABOUT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ABOUT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ROOT_URI));

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(reporter).report(eq(UriFixtures.MONZO_ABOUT_URI), eq(Set.of()), anyInt());
    }
}
