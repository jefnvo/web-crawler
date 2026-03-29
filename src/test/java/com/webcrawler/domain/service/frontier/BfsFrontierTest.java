package com.webcrawler.domain.service.frontier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.webcrawler.fixtures.Uris;

public class BfsFrontierTest {

    private final Frontier frontier = new BfsFrontier();

    @Test
    void shouldBeEmptyInitially() {
        assertTrue(frontier.isEmpty());
    }

    @Test
    void shouldReturnEmptyOptionalWhenPolledEmpty() {
        assertTrue(frontier.poll().isEmpty());
    }

    @Test
    void shouldNotBeEmptyAfterAdd() {
        frontier.add(Uris.PAGE_A);
        assertTrue(!frontier.isEmpty());
    }

    @Test
    void shouldReturnUriInFifoOrder() {
        frontier.add(Uris.PAGE_A);
        frontier.add(Uris.PAGE_B);
        frontier.add(Uris.PAGE_C);

        assertEquals(Uris.PAGE_A, frontier.poll().orElseThrow());
        assertEquals(Uris.PAGE_B, frontier.poll().orElseThrow());
        assertEquals(Uris.PAGE_C, frontier.poll().orElseThrow());
    }

    @Test
    void shouldBeEmptyAfterAllElementsPolled() {
        frontier.add(Uris.PAGE_A);
        frontier.poll();
        assertTrue(frontier.isEmpty());
    }
}
