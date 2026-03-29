package com.webcrawler;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.webcrawler.domain.port.out.ResultReporter;
import com.webcrawler.domain.service.CrawlController;
import com.webcrawler.domain.service.DefaultPageProcessor;
import com.webcrawler.domain.service.frontier.ConcurrentBfsFrontier;
import com.webcrawler.domain.service.PageProcessor;
import com.webcrawler.domain.service.strategy.ConcurrentCrawlStrategy;
import com.webcrawler.infra.adapter.out.http.HttpPageFetcher;
import com.webcrawler.infra.adapter.out.parser.JsoupLinkExtractor;
import com.webcrawler.infra.config.HttpClientConfig;
import com.webcrawler.fixtures.HtmlPages;

@WireMockTest
class CrawlerIntegrationTest {

    private static final HttpClientConfig TEST_HTTP_CONFIG = new HttpClientConfig(
            Duration.ofSeconds(5), Duration.ofSeconds(5), "TestCrawler/1.0", 0, 0L);


    private CrawlController crawlerWith(CapturingResultReporter reporter) {
        var fetcher = new HttpPageFetcher(TEST_HTTP_CONFIG);
        var extractor = new JsoupLinkExtractor();
        PageProcessor pageProcessor = new DefaultPageProcessor(fetcher, extractor);
        
        var strategy = new ConcurrentCrawlStrategy(pageProcessor, reporter, 1, Integer.MAX_VALUE, ConcurrentBfsFrontier::new);
        return new CrawlController(strategy);
    }

    @Test
    @Timeout(10)
    void shouldCrawlAllReachablePagesWithinSameSubdomain(WireMockRuntimeInfo wm) {
        stubFor(get("/").willReturn(ok(HtmlPages.htmlWithLinks("/about", "/contact"))));
        stubFor(get("/about").willReturn(ok(HtmlPages.htmlWithLinks("/faq"))));
        stubFor(get("/contact").willReturn(ok(HtmlPages.htmlWithLinks())));
        stubFor(get("/faq").willReturn(ok(HtmlPages.htmlWithLinks())));

        var reporter = new CapturingResultReporter();
        crawlerWith(reporter).crawl(wm.getHttpBaseUrl() + "/");

        var visited = reporter.visitedPages();
        var base    = wm.getHttpBaseUrl();
        assertAll(
            () -> assertTrue(visited.contains(URI.create(base + "/")),        "root must be visited"),
            () -> assertTrue(visited.contains(URI.create(base + "/about")),   "/about must be visited"),
            () -> assertTrue(visited.contains(URI.create(base + "/contact")), "/contact must be visited"),
            () -> assertTrue(visited.contains(URI.create(base + "/faq")),     "/faq reachable via /about must be visited"),
            () -> assertEquals(4, visited.size(),                             "exactly 4 distinct pages in the site graph")
        );
    }

    @Test
    @Timeout(10)
    void shouldNotFetchPagesOutsideStartingSubdomain(WireMockRuntimeInfo wm) {
        stubFor(get("/").willReturn(ok(HtmlPages.htmlWithLinks(
                "/internal",
                "http://external.example.com/page"   // different host — out of scope
        ))));
        stubFor(get("/internal").willReturn(ok(HtmlPages.htmlWithLinks())));

        var reporter = new CapturingResultReporter();
        crawlerWith(reporter).crawl(wm.getHttpBaseUrl() + "/");

        var visited = reporter.visitedPages();
        assertAll(
            () -> assertTrue(
                    visited.stream().noneMatch(u -> "external.example.com".equals(u.getHost())),
                    "out-of-scope host must never appear in the visited set"),
            () -> assertEquals(2, visited.size(), "only root and /internal should be crawled")
        );
    }

    @Test
    @Timeout(10)
    void shouldVisitEachPageExactlyOnce(WireMockRuntimeInfo wm) {
        // Both /a and /b link to /shared — concurrent discovery must not double-fetch it.
        stubFor(get("/").willReturn(ok(HtmlPages.htmlWithLinks("/a", "/b"))));
        stubFor(get("/a").willReturn(ok(HtmlPages.htmlWithLinks("/shared"))));
        stubFor(get("/b").willReturn(ok(HtmlPages.htmlWithLinks("/shared"))));
        stubFor(get("/shared").willReturn(ok(HtmlPages.htmlWithLinks())));

        crawlerWith(new CapturingResultReporter()).crawl(wm.getHttpBaseUrl() + "/");

        verify(1, getRequestedFor(urlEqualTo("/shared")));
    }

    @Test
    @Timeout(10)
    void shouldContinueCrawlingAfterFetchFailure(WireMockRuntimeInfo wm) {
        stubFor(get("/").willReturn(ok(HtmlPages.htmlWithLinks("/good", "/bad"))));
        stubFor(get("/good").willReturn(ok(HtmlPages.htmlWithLinks())));
        stubFor(get("/bad").willReturn(aResponse().withStatus(404)));

        var reporter = new CapturingResultReporter();
        assertDoesNotThrow(() -> crawlerWith(reporter).crawl(wm.getHttpBaseUrl() + "/"));

        assertTrue(
            reporter.visitedPages().contains(URI.create(wm.getHttpBaseUrl() + "/good")),
            "/good must be crawled even when /bad fails"
        );
    }

    @Test
    @Timeout(10)
    void shouldNormalizeUrisAndDeduplicateBeforeEnqueuing(WireMockRuntimeInfo wm) {
        stubFor(get("/").willReturn(ok(HtmlPages.htmlWithLinks(
                "/page/",      
                "/page#section" 
        ))));
        stubFor(get("/page").willReturn(ok(HtmlPages.htmlWithLinks())));

        crawlerWith(new CapturingResultReporter()).crawl(wm.getHttpBaseUrl() + "/");

        verify(1, getRequestedFor(urlEqualTo("/page")));
    }
    
    private static final class CapturingResultReporter implements ResultReporter {

        private final ConcurrentHashMap<URI, Set<URI>> reports = new ConcurrentHashMap<>();

        @Override
        public void report(URI visited, Set<URI> links, int depth) {
            reports.put(visited, links);
        }

        Set<URI> visitedPages() {
            return reports.keySet();
        }

    }
}
