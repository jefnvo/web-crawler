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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.webcrawler.domain.port.out.PageFetchException;
import com.webcrawler.domain.port.out.ResultReporter;
import com.webcrawler.domain.service.PageProcessor;
import com.webcrawler.fixtures.UriFixtures;

public class ConcurrentCrawlStrategyTest {

    private final PageProcessor processor = mock(PageProcessor.class);
    private final ResultReporter reporter = mock(ResultReporter.class);

    private final CrawlStrategy strategy =
            new ConcurrentCrawlStrategy(processor, reporter, 10);

    @Test
    void shouldVisitStartPageAndReportIt() {
        when(processor.fetchLinks(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of());

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);
        verify(reporter).report(eq(UriFixtures.MONZO_ROOT_URI), eq(Set.of()), anyInt());
    }

    @Test
    void shouldFollowInScopeLinks() {
        when(processor.fetchLinks(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ABOUT_URI));
        when(processor.fetchLinks(eq(UriFixtures.MONZO_ABOUT_URI), any())).thenReturn(Set.of());

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(processor, times(1)).fetchLinks(eq(UriFixtures.MONZO_ROOT_URI), any());
        verify(processor, times(1)).fetchLinks(eq(UriFixtures.MONZO_ABOUT_URI), any());
    }

    @Test
    void shouldNotFollowOutOfScopeLinks() {
        var external = URI.create("https://facebook.com/");
        when(processor.fetchLinks(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of());

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(processor, never()).fetchLinks(eq(external), any());
    }

    @Test
    void shouldNotRevisitAlreadyVisitedPages() {
        when(processor.fetchLinks(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ABOUT_URI));
        when(processor.fetchLinks(eq(UriFixtures.MONZO_ABOUT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ROOT_URI));

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(processor, times(1)).fetchLinks(eq(UriFixtures.MONZO_ROOT_URI), any());
        verify(processor, times(1)).fetchLinks(eq(UriFixtures.MONZO_ABOUT_URI), any());
    }

    @Test
    void shouldContinueWhenAPageFetchFails() {
        when(processor.fetchLinks(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ABOUT_URI));
        when(processor.fetchLinks(eq(UriFixtures.MONZO_ABOUT_URI), any())).thenThrow(new PageFetchException("timeout"));

        assertDoesNotThrow(() -> strategy.crawl(UriFixtures.MONZO_ROOT_URI));
        verify(processor, times(1)).fetchLinks(eq(UriFixtures.MONZO_ABOUT_URI), any());
    }

    @Test
    void shouldReportOnlyNewlyDiscoveredLinks() {
        when(processor.fetchLinks(eq(UriFixtures.MONZO_ROOT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ABOUT_URI));
        when(processor.fetchLinks(eq(UriFixtures.MONZO_ABOUT_URI), any())).thenReturn(Set.of(UriFixtures.MONZO_ROOT_URI));

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(reporter).report(eq(UriFixtures.MONZO_ABOUT_URI), eq(Set.of()), anyInt());
    }

    @Timeout(5)
    @Test
    void shouldFetchDiscoveredPagesConcurrently() throws InterruptedException {
        var bothInFlight = new CountDownLatch(2);

        when(processor.fetchLinks(eq(UriFixtures.MONZO_ROOT_URI), any()))
            .thenReturn(Set.of(UriFixtures.MONZO_ABOUT_URI, UriFixtures.MONZO_BLOG_URI));

        when(processor.fetchLinks(eq(UriFixtures.MONZO_ABOUT_URI), any())).thenAnswer(inv -> {
            bothInFlight.countDown();
            bothInFlight.await();
            return Set.of();
        });
        when(processor.fetchLinks(eq(UriFixtures.MONZO_BLOG_URI), any())).thenAnswer(inv -> {
            bothInFlight.countDown();
            bothInFlight.await();
            return Set.of();
        });

        strategy.crawl(UriFixtures.MONZO_ROOT_URI);

        verify(processor, times(1)).fetchLinks(eq(UriFixtures.MONZO_ABOUT_URI), any());
        verify(processor, times(1)).fetchLinks(eq(UriFixtures.MONZO_BLOG_URI), any());
    }
}
