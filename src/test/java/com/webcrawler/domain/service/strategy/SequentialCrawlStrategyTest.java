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
import com.webcrawler.fixtures.Uris;

public class SequentialCrawlStrategyTest {

    private final PageFetcher fetcher     = mock(PageFetcher.class);
    private final LinkExtractor extractor = mock(LinkExtractor.class);
    private final ResultReporter reporter = mock(ResultReporter.class);

    private final CrawlStrategy strategy =
            new SequentialCrawlStrategy(fetcher, extractor, reporter, new BfsFrontier());

    @Test
    void shouldVisitOnceSinglePage() {
        when(fetcher.fetch(Uris.MONZO_ROOT)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(Uris.MONZO_ROOT, HtmlPages.MINIMAL)).thenReturn(Set.of());

        strategy.crawl(Uris.MONZO_ROOT);

        verify(fetcher, times(1)).fetch(Uris.MONZO_ROOT);
        verify(reporter, times(1)).report(eq(Uris.MONZO_ROOT), eq(Set.of()), anyInt());
    }

    @Test
    void shouldFollowInScopeLink() {
        when(fetcher.fetch(Uris.MONZO_ROOT)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(Uris.MONZO_ROOT), any())).thenReturn(Set.of(Uris.MONZO_ABOUT));
        when(fetcher.fetch(Uris.MONZO_ABOUT)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(Uris.MONZO_ABOUT), any())).thenReturn(Set.of());

        strategy.crawl(Uris.MONZO_ROOT);

        verify(fetcher, times(1)).fetch(Uris.MONZO_ROOT);
        verify(fetcher, times(1)).fetch(Uris.MONZO_ABOUT);
    }

    @Test
    void shouldNotFollowOutOfScopeLinks() {
        var external = URI.create("https://facebook.com/");
        when(fetcher.fetch(Uris.MONZO_ROOT)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(Uris.MONZO_ROOT), any())).thenReturn(Set.of(external));

        strategy.crawl(Uris.MONZO_ROOT);

        verify(fetcher, never()).fetch(external);
    }

    @Test
    void shouldNotFollowNonHttpLinks() {
        var mailto = URI.create("mailto:support@example.com");
        when(fetcher.fetch(Uris.MONZO_ROOT)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(Uris.MONZO_ROOT), any())).thenReturn(Set.of(mailto));

        strategy.crawl(Uris.MONZO_ROOT);

        verify(fetcher, never()).fetch(mailto);
    }

    @Test
    void shouldNotRevisitAlreadyVisitedPages() {
        when(fetcher.fetch(Uris.MONZO_ROOT)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(Uris.MONZO_ROOT), any())).thenReturn(Set.of(Uris.MONZO_ABOUT));
        when(fetcher.fetch(Uris.MONZO_ABOUT)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(Uris.MONZO_ABOUT), any())).thenReturn(Set.of(Uris.MONZO_ROOT));

        strategy.crawl(Uris.MONZO_ROOT);

        verify(fetcher, times(1)).fetch(Uris.MONZO_ROOT);
        verify(fetcher, times(1)).fetch(Uris.MONZO_ABOUT);
    }

    @Test
    void shouldContinueWhenPageFetchFails() {
        when(fetcher.fetch(Uris.MONZO_ROOT)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(Uris.MONZO_ROOT), any())).thenReturn(Set.of(Uris.MONZO_ABOUT));
        when(fetcher.fetch(Uris.MONZO_ABOUT)).thenThrow(new PageFetchException("timeout"));

        assertDoesNotThrow(() -> strategy.crawl(Uris.MONZO_ROOT));
        verify(fetcher, times(1)).fetch(Uris.MONZO_ABOUT);
    }

    @Test
    void shouldReportOnlyNewlyDiscoveredLinks() {
        when(fetcher.fetch(Uris.MONZO_ROOT)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(Uris.MONZO_ROOT), any())).thenReturn(Set.of(Uris.MONZO_ABOUT));
        when(fetcher.fetch(Uris.MONZO_ABOUT)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(Uris.MONZO_ABOUT), any())).thenReturn(Set.of(Uris.MONZO_ROOT));

        strategy.crawl(Uris.MONZO_ROOT);

        verify(reporter).report(eq(Uris.MONZO_ABOUT), eq(Set.of()), anyInt());
    }
}
