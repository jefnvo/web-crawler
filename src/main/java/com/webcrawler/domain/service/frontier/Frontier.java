package com.webcrawler.domain.service.frontier;

import java.net.URI;
import java.util.Optional;

/**
 * Abstracts the URL ordering policy for a crawl.
 * Implementations determine traversal order (BFS, DFS, priority-based, hybrid)
 * independently of the crawl execution model (sequential or concurrent).
 */
public interface Frontier {
    void add(URI uri);
    Optional<URI> poll();
    boolean isEmpty();
}
