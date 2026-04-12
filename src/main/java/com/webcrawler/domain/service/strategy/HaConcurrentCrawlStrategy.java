package com.webcrawler.domain.service.strategy;

import com.webcrawler.domain.port.out.ResultReporter;
import com.webcrawler.domain.service.PageProcessor;
import com.webcrawler.domain.service.frontier.Frontier;
import com.webcrawler.domain.util.CrawlScope;
import com.webcrawler.domain.util.UriNormalizer;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HaConcurrentCrawlStrategy implements CrawlStrategy {

    private static final Logger LOG = Logger.getLogger(HaConcurrentCrawlStrategy.class.getName());

    private final PageProcessor processor;
    private final ResultReporter reporter;
    private final int maxConcurrency;
    private final int maxPages;
    private final Supplier<Frontier> frontierFactory;

    public HaConcurrentCrawlStrategy(PageProcessor processor, ResultReporter reporter,
                                     int maxConcurrency, int maxPages, Supplier<Frontier> frontierFactory) {
        this.processor = processor;
        this.reporter = reporter;
        this.maxConcurrency = maxConcurrency;
        this.maxPages = maxPages;
        this.frontierFactory = frontierFactory;
    }

    @Override
    public void crawl(URI start) {
        var normalized = UriNormalizer.normalize(start);
        var scope = new CrawlScope(normalized.getHost());
        var frontier = frontierFactory.get();
        var inFlight = new AtomicInteger(1);
        var pagesVisited = new AtomicInteger(0);

        frontier.offer(normalized, 0);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < maxConcurrency; i++) {
                executor.submit(() -> runWorker(frontier, scope, inFlight, pagesVisited));
            }
        }

        reporter.summarize();
    }

    private void runWorker(Frontier frontier, CrawlScope scope,
                           AtomicInteger inFlight, AtomicInteger pagesVisited) {
        URI uri;
        while ((uri = frontier.pickWork()) != null) {
            processPage(uri, frontier, scope, inFlight, pagesVisited);
        }
    }

    private void processPage(URI uri, Frontier frontier, CrawlScope scope,
                              AtomicInteger inFlight, AtomicInteger pagesVisited) {
        int depth = frontier.depthOf(uri);
        try {
            if (pagesVisited.getAndIncrement() < maxPages) {
                var newLinks = processor.fetchLinks(uri, scope).stream()
                    .filter(link -> {
                        if (frontier.offer(link, depth + 1)) {
                            inFlight.incrementAndGet();
                            return true;
                        }
                        return false;
                    })
                    .collect(Collectors.toUnmodifiableSet());
                reporter.report(uri, newLinks, depth);
            }
        } catch (Exception ex) {
            LOG.warning("Failed: %s — %s".formatted(uri, ex.getMessage()));
        } finally {
            if (inFlight.decrementAndGet() == 0) {
                frontier.close();
            }
        }
    }
}
