package com.webcrawler.infra.adapter.out.http;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.webcrawler.domain.port.out.PageFetchException;
import com.webcrawler.infra.config.HttpClientConfig;

@WireMockTest
class HttpPageFetcherTest {

    private static final HttpClientConfig FAST_CONFIG = new HttpClientConfig(
            Duration.ofSeconds(5), Duration.ofSeconds(5), "TestCrawler/1.0", 3, 0L);

    private HttpPageFetcher fetcher() {
        return new HttpPageFetcher(FAST_CONFIG);
    }

    private URI url(WireMockRuntimeInfo wm, String path) {
        return URI.create(wm.getHttpBaseUrl() + path);
    }

    @Test
    void shouldReturnBodyOn200(WireMockRuntimeInfo wm) {
        stubFor(get("/page").willReturn(ok("hello")));

        var body = fetcher().fetch(url(wm, "/page"));

        assertEquals("hello", body);
    }

    @Test
    void shouldThrowImmediatelyOnNonRetryableStatus(WireMockRuntimeInfo wm) {
        stubFor(get("/page").willReturn(aResponse().withStatus(404)));

        assertThrows(PageFetchException.class, () -> fetcher().fetch(url(wm, "/page")));

        verify(1, getRequestedFor(urlEqualTo("/page")));
    }

    @Test
    void shouldRetryOnRetryableStatusAndEventuallySucceed(WireMockRuntimeInfo wm) {
        stubFor(get("/page").inScenario("retry")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("second"));
        stubFor(get("/page").inScenario("retry")
                .whenScenarioStateIs("second")
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("third"));
        stubFor(get("/page").inScenario("retry")
                .whenScenarioStateIs("third")
                .willReturn(ok("recovered")));

        var body = fetcher().fetch(url(wm, "/page"));

        assertEquals("recovered", body);
        verify(3, getRequestedFor(urlEqualTo("/page")));
    }

    @Test
    void shouldThrowAfterMaxRetriesExhausted(WireMockRuntimeInfo wm) {
        stubFor(get("/page").willReturn(aResponse().withStatus(503)));

        assertThrows(PageFetchException.class, () -> fetcher().fetch(url(wm, "/page")));

        verify(4, getRequestedFor(urlEqualTo("/page")));
    }
}
