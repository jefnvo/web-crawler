package com.webcrawler.domain.service.frontier;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class BfsFrontier implements Frontier {

    private final Deque<URI> queue = new ArrayDeque<>();

    @Override
    public void add(URI uri) {
        queue.add(uri);
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
