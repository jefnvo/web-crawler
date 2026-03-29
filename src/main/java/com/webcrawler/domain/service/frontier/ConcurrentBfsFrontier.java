package com.webcrawler.domain.service.frontier;

import java.net.URI;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentBfsFrontier implements Frontier {

    private final Queue<URI> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void add(URI uri) {
        queue.offer(uri);
    }

    @Override
    public Optional<URI> poll() {
        return Optional.ofNullable(queue.poll());
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
