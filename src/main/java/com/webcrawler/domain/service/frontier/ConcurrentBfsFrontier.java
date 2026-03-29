package com.webcrawler.domain.service.frontier;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentBfsFrontier implements Frontier {

    private final ConcurrentHashMap<URI, Integer> depths = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<URI> queue = new ConcurrentLinkedQueue<>();

    @Override
    public boolean offer(URI uri, int depth) {
        if (depths.putIfAbsent(uri, depth) != null) {
            return false;
        }
        queue.add(uri);
        return true;
    }

    @Override
    public URI poll() {
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
