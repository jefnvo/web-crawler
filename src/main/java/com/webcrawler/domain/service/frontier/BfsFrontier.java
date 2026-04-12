package com.webcrawler.domain.service.frontier;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class BfsFrontier implements Frontier {

    private final Map<URI, Integer> depths = new HashMap<>();
    private final Deque<URI> queue = new ArrayDeque<>();

    @Override
    public boolean offer(URI uri, int depth) {
        if (depths.putIfAbsent(uri, depth) != null) {
            return false;
        }
        queue.add(uri);
        return true;
    }

    @Override
    public URI pickWork() {
        return queue.poll();
    }

    @Override
    public int depthOf(URI uri) {
        return depths.getOrDefault(uri, 0);
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
