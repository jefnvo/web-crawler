package com.webcrawler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CrawlArgsTest {

    @Test
    void shouldParseUrlOnlyAsSequential() {
        var args = CrawlArgs.parse(new String[]{"https://example.com"});

        assertEquals("https://example.com", args.url());
        assertFalse(args.concurrent());
        assertEquals(CrawlArgs.DEFAULT_MAX_CONCURRENCY, args.maxConcurrentRequests());
    }

    @Test
    void shouldParseFasterModeAsConcurrentWithDefaultThreads() {
        var args = CrawlArgs.parse(new String[]{"https://example.com", "faster"});

        assertTrue(args.concurrent());
        assertEquals(CrawlArgs.DEFAULT_MAX_CONCURRENCY, args.maxConcurrentRequests());
    }

    @Test
    void shouldParseFasterModeWithExplicitThreadCount() {
        var args = CrawlArgs.parse(new String[]{"https://example.com", "faster", "50"});

        assertTrue(args.concurrent());
        assertEquals(50, args.maxConcurrentRequests());
    }

    @Test
    void shouldThrowWhenNoArgsProvided() {
        assertThrows(IllegalArgumentException.class, () -> CrawlArgs.parse(new String[]{}));
    }

    @Test
    void shouldThrowWhenArgsIsNull() {
        assertThrows(IllegalArgumentException.class, () -> CrawlArgs.parse(null));
    }

    @Test
    void shouldThrowWhenThreadCountIsNotANumber() {
        assertThrows(IllegalArgumentException.class,
                () -> CrawlArgs.parse(new String[]{"https://example.com", "faster", "abc"}));
    }

    @Test
    void shouldThrowWhenThreadCountIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> CrawlArgs.parse(new String[]{"https://example.com", "faster", "0"}));
        assertThrows(IllegalArgumentException.class,
                () -> CrawlArgs.parse(new String[]{"https://example.com", "faster", "-5"}));
    }

    @Test
    void shouldThrowWhenModeIsUnknown() {
        assertThrows(IllegalArgumentException.class,
                () -> CrawlArgs.parse(new String[]{"https://example.com", "turbo"}));
    }
}
