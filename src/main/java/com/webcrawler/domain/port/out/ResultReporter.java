package com.webcrawler.domain.port.out;

import java.net.URI;
import java.util.Set;

public interface ResultReporter {
    void report(URI visited, Set<URI> links, int depth);
    default void summarize() {}
}
