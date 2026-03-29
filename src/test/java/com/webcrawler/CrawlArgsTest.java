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
        var args = CrawlArgs.parse(new String[]{"https://example.com", "faster", "--threads", "50"});

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
                () -> CrawlArgs.parse(new String[]{"https://example.com", "faster", "--threads", "abc"}));
    }

    @Test
    void shouldThrowWhenThreadCountIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> CrawlArgs.parse(new String[]{"https://example.com", "faster", "--threads", "0"}));
        assertThrows(IllegalArgumentException.class,
                () -> CrawlArgs.parse(new String[]{"https://example.com", "faster", "--threads", "-5"}));
    }

    @Test
    void shouldThrowWhenThreadsFlagHasNoValue() {
        assertThrows(IllegalArgumentException.class,
                () -> CrawlArgs.parse(new String[]{"https://example.com", "faster", "--threads"}));
    }

    @Test
    void shouldThrowWhenModeIsUnknown() {
        assertThrows(IllegalArgumentException.class,
                () -> CrawlArgs.parse(new String[]{"https://example.com", "turbo"}));
    }

    @Test
    void shouldDefaultToUnlimitedMaxPages() {
        var args = CrawlArgs.parse(new String[]{"https://example.com"});

        assertEquals(CrawlArgs.DEFAULT_MAX_PAGES, args.maxPages());
    }

    @Test
    void shouldParseMaxPagesFlag() {
        var args = CrawlArgs.parse(new String[]{"https://example.com", "--max-pages", "100"});

        assertEquals(100, args.maxPages());
    }

    @Test
    void shouldParseMaxPagesWithFaster() {
        var args = CrawlArgs.parse(new String[]{"https://example.com", "faster", "--max-pages", "50"});

        assertTrue(args.concurrent());
        assertEquals(50, args.maxPages());
    }

    @Test
    void shouldParseMaxPagesWithFasterAndThreads() {
        var args = CrawlArgs.parse(new String[]{"https://example.com", "faster", "--threads", "10", "--max-pages", "200"});

        assertTrue(args.concurrent());
        assertEquals(10, args.maxConcurrentRequests());
        assertEquals(200, args.maxPages());
    }

    @Test
    void shouldThrowWhenMaxPagesFlagHasNoValue() {
        assertThrows(IllegalArgumentException.class,
                () -> CrawlArgs.parse(new String[]{"https://example.com", "--max-pages"}));
    }

    @Test
    void shouldThrowWhenMaxPagesIsNotANumber() {
        assertThrows(IllegalArgumentException.class,
                () -> CrawlArgs.parse(new String[]{"https://example.com", "--max-pages", "abc"}));
    }

    @Test
    void shouldThrowWhenMaxPagesIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> CrawlArgs.parse(new String[]{"https://example.com", "--max-pages", "0"}));
        assertThrows(IllegalArgumentException.class,
                () -> CrawlArgs.parse(new String[]{"https://example.com", "--max-pages", "-5"}));
    }
}
