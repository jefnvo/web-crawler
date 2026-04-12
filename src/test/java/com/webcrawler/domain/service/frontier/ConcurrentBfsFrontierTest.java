package com.webcrawler.domain.service.frontier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.webcrawler.fixtures.UriFixtures;

public class ConcurrentBfsFrontierTest {

    private Frontier frontier;

    @BeforeEach
    void setUp() {
        frontier = new ConcurrentBfsFrontier();
    }

    @Test
    void shouldStartEmpty() {
        assertTrue(frontier.isEmpty());
        assertNull(frontier.pickWork());
    }

    @Test
    void shouldReturnTrueWhenOfferingNewUri() {
        assertTrue(frontier.offer(UriFixtures.PAGE, 0));
    }

    @Test
    void shouldReturnFalseForDuplicateUri() {
        frontier.offer(UriFixtures.PAGE, 0);
        assertFalse(frontier.offer(UriFixtures.PAGE, 0));
    }

    @Test
    void shouldNotEnqueueDuplicateUri() {
        frontier.offer(UriFixtures.PAGE, 0);
        frontier.offer(UriFixtures.PAGE, 0);
        frontier.pickWork();
        assertTrue(frontier.isEmpty());
    }

    @Test
    void shouldTrackDepthOfOfferedUri() {
        frontier.offer(UriFixtures.PAGE, 3);
        assertEquals(3, frontier.depthOf(UriFixtures.PAGE));
    }

    @Timeout(5)
    @RepeatedTest(20)
    void shouldNotLoseUrisUnderConcurrentOffers() throws InterruptedException {
        int threadCount = 50;
        List<URI> uris = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            uris.add(URI.create("https://crawlme.monzo.com/page-" + i));
        }

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var uri : uris) {
                executor.submit(() -> frontier.offer(uri, 1));
            }
        }

        int count = 0;
        while (!frontier.isEmpty()) {
            frontier.pickWork();
            count++;
        }
        assertEquals(threadCount, count);
    }

    @Timeout(5)
    @RepeatedTest(20)
    void shouldDeduplicateUnderConcurrentOffersOfSameUri() throws InterruptedException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 50; i++) {
                executor.submit(() -> frontier.offer(UriFixtures.PAGE, 0));
            }
        }

        frontier.pickWork();
        assertTrue(frontier.isEmpty());
    }
}
