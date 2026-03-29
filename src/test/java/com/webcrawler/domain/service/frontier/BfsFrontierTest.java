package com.webcrawler.domain.service.frontier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webcrawler.fixtures.UriFixtures;

public class BfsFrontierTest {

    private Frontier frontier;

    @BeforeEach
    void setUp() {
        frontier = new BfsFrontier();
    }

    @Test
    void shouldStartEmpty() {
        assertTrue(frontier.isEmpty());
        assertNull(frontier.poll());
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
    void shouldNotBeEmptyAfterOffer() {
        frontier.offer(UriFixtures.PAGE, 0);
        assertFalse(frontier.isEmpty());
    }

    @Test
    void shouldPollOfferedUri() {
        frontier.offer(UriFixtures.PAGE, 0);
        assertEquals(UriFixtures.PAGE, frontier.poll());
    }

    @Test
    void shouldBeEmptyAfterPollingAll() {
        frontier.offer(UriFixtures.PAGE, 0);
        frontier.poll();
        assertTrue(frontier.isEmpty());
    }

    @Test
    void shouldNotEnqueueDuplicateUri() {
        frontier.offer(UriFixtures.PAGE, 0);
        frontier.offer(UriFixtures.PAGE, 0);
        frontier.poll();
        assertTrue(frontier.isEmpty());
    }

    @Test
    void shouldTrackDepthOfOfferedUri() {
        frontier.offer(UriFixtures.PAGE, 3);
        assertEquals(3, frontier.depthOf(UriFixtures.PAGE));
    }

    @Test
    void shouldPreserveFifoOrder() {
        frontier.offer(UriFixtures.PAGE_A, 0);
        frontier.offer(UriFixtures.PAGE_B, 0);
        frontier.offer(UriFixtures.PAGE_C, 0);

        assertEquals(UriFixtures.PAGE_A, frontier.poll());
        assertEquals(UriFixtures.PAGE_B, frontier.poll());
        assertEquals(UriFixtures.PAGE_C, frontier.poll());
    }

    @Test
    void shouldRejectDuplicateEvenAtDifferentDepth() {
        frontier.offer(UriFixtures.PAGE, 0);
        assertFalse(frontier.offer(UriFixtures.PAGE, 1));
        frontier.poll();
        assertTrue(frontier.isEmpty());
    }
}
