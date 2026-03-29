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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.webcrawler.domain.port.out.LinkExtractor;
import com.webcrawler.domain.port.out.PageFetchException;
import com.webcrawler.domain.port.out.PageFetcher;
import com.webcrawler.domain.port.out.ResultReporter;
import com.webcrawler.domain.service.frontier.ConcurrentBfsFrontier;
import com.webcrawler.fixtures.HtmlPages;
import com.webcrawler.fixtures.UriFixtures;

public class ConcurrentCrawlStrategyTest {

    private final PageFetcher fetcher     = mock(PageFetcher.class);
    private final LinkExtractor extractor = mock(LinkExtractor.class);
    private final ResultReporter reporter = mock(ResultReporter.class);

    private final CrawlStrategy strategy =
            new ConcurrentCrawlStrategy(fetcher, extractor, reporter, new ConcurrentBfsFrontier());

    @Test
    void shouldVisitStartPageAndReportIt() {
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of());

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);
        verify(reporter).report(eq(UriFixtures.MONZO_ROOT_URI), eq(Set.of()), anyInt());
    }

    @Test
    void shouldFollowInScopeLinks() {
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ABOUT_URI));
        when(fetcher.fetch(UriFixtures.MONZO_ABOUT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ABOUT_URI), any())).thenReturn(Set.of());
        
        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(fetcher, times(1)).fetch(UriFixtures.MONZO_ROOT_URI);
        verify(fetcher, times(1)).fetch(UriFixtures.MONZO_ABOUT_URI);
    }

    @Test
    void shouldNotFollowOutOfScopeLinks() {
        var external = URI.create("https://facebook.com/");
        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of(external));

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(fetcher, never()).fetch(external);
    }

    @RepeatedTest(30)
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
    void shouldContinueWhenAPageFetchFails() {
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

    @Timeout(5)
    @RepeatedTest(30)
    void shouldFetchDiscoveredPagesConcurrently() throws InterruptedException {
        var bothInFlight = new CountDownLatch(2);

        when(fetcher.fetch(UriFixtures.MONZO_ROOT_URI)).thenReturn(HtmlPages.MINIMAL);
        when(extractor.extract(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ABOUT_URI, UriFixtures.MONZO_BLOG_URI));

        when(fetcher.fetch(UriFixtures.MONZO_ABOUT_URI)).thenAnswer(inv -> {
            bothInFlight.countDown();
            bothInFlight.await(2, TimeUnit.SECONDS);
            return HtmlPages.MINIMAL;
        });
        when(fetcher.fetch(UriFixtures.MONZO_BLOG_URI)).thenAnswer(inv -> {
            bothInFlight.countDown();
            bothInFlight.await(2, TimeUnit.SECONDS);
            return HtmlPages.MINIMAL;
        });
        when(extractor.extract(eq(UriFixtures.MONZO_ABOUT_URI), any())).thenReturn(Set.of());
        when(extractor.extract(eq(UriFixtures.MONZO_BLOG_URI), any())).thenReturn(Set.of());

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(fetcher, times(1)).fetch(UriFixtures.MONZO_ABOUT_URI);
        verify(fetcher, times(1)).fetch(UriFixtures.MONZO_BLOG_URI);
    }
}
