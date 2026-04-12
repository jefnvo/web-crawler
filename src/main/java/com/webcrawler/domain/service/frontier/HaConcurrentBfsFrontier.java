package com.webcrawler.domain.service.frontier;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class HaConcurrentBfsFrontier implements Frontier {

    private static final URI SENTINEL = URI.create("urn:sentinel");

    private final ConcurrentHashMap<URI, Integer> depths = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<URI> queue  = new LinkedBlockingQueue<>();

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
        try {
            URI uri = queue.take();
            if (uri == SENTINEL) {
                queue.offer(SENTINEL);
                return null;
            }
            return uri;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void close() {
        queue.offer(SENTINEL);
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
