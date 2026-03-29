package com.webcrawler.domain.service.frontier;

import java.net.URI;

public interface Frontier {

    boolean offer(URI uri, int depth);

    URI poll();
    
    int depthOf(URI uri);

    boolean isEmpty();
}
