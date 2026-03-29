package com.webcrawler.infra.adapter.out.reporter;

import com.webcrawler.domain.port.out.ResultReporter;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class InstrumentedReporter implements ResultReporter {

    private final ResultReporter delegate;
    private final AtomicInteger pageCount = new AtomicInteger();
    private final AtomicInteger maxDepth  = new AtomicInteger();

    public InstrumentedReporter(ResultReporter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void report(URI visited, Set<URI> links, int depth) {
        pageCount.incrementAndGet();
        maxDepth.updateAndGet(current -> Math.max(current, depth));
        delegate.report(visited, links, depth);
    }

    @Override
    public void summarize() {
        delegate.summarize();
        System.out.println("----------------------------------");
        System.out.println("Pages visited : " + pageCount.get());
        System.out.println("Max depth     : " + maxDepth.get());
        System.out.println("----------------------------------");
    }
}
