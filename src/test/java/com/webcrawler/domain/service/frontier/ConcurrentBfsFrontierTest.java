package com.webcrawler.domain.service.frontier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.webcrawler.fixtures.Uris;

public class ConcurrentBfsFrontierTest {

    private final Frontier frontier = new ConcurrentBfsFrontier();

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
    void shouldBeEmptyAfterAllElementsPolled() {
        frontier.add(Uris.PAGE_A);
        frontier.poll();
        assertTrue(frontier.isEmpty());
    }

    @RepeatedTest(20)
    void shouldAcceptConcurrentAddsWithoutDataLoss() throws InterruptedException {
        var uris = List.of(Uris.PAGE_A, Uris.PAGE_B, Uris.PAGE_C);
        var latch = new CountDownLatch(1);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var uri : uris) {
                executor.submit(() -> {
                    latch.await();
                    frontier.add(uri);
                    return null;
                });
            }
            latch.countDown();
        }

        List<URI> polled = new ArrayList<>();
        frontier.poll().ifPresent(polled::add);
        frontier.poll().ifPresent(polled::add);
        frontier.poll().ifPresent(polled::add);

        assertEquals(3, polled.size());
        assertTrue(polled.containsAll(uris));
    }
}
