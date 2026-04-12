package com.webcrawler.domain.service.frontier;

import java.net.URI;

public interface Frontier {

    boolean offer(URI uri, int depth);

    URI pickWork();

    int depthOf(URI uri);

    boolean isEmpty();

    default void close() {}
}
