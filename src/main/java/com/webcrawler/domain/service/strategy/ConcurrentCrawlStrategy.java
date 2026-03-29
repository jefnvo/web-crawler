package com.webcrawler.domain.service.strategy;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.webcrawler.domain.port.out.LinkExtractor;
import com.webcrawler.domain.port.out.PageFetcher;
import com.webcrawler.domain.port.out.ResultReporter;
import com.webcrawler.domain.service.frontier.Frontier;
import com.webcrawler.domain.util.CrawlScope;
import com.webcrawler.domain.util.UriNormalizer;

/**
 * swap ConcurrentBfsFrontier for any other thread-safe Frontier without touching this class.
 */
public class ConcurrentCrawlStrategy implements CrawlStrategy {

    private static final Logger LOG = Logger.getLogger(ConcurrentCrawlStrategy.class.getName());
    public  static final int DEFAULT_MAX_CONCURRENCY = 1000;

    private final PageFetcher fetcher;
    private final LinkExtractor extractor;
    private final ResultReporter reporter;
    private final Frontier frontier;
    private final int maxConcurrency;

    public ConcurrentCrawlStrategy(PageFetcher fetcher, LinkExtractor extractor,
                                   ResultReporter reporter, Frontier frontier) {
        this(fetcher, extractor, reporter, frontier, DEFAULT_MAX_CONCURRENCY);
    }

    public ConcurrentCrawlStrategy(PageFetcher fetcher, LinkExtractor extractor,
                                   ResultReporter reporter, Frontier frontier, int maxConcurrency) {
        this.fetcher        = fetcher;
        this.extractor      = extractor;
        this.reporter       = reporter;
        this.frontier       = frontier;
        this.maxConcurrency = maxConcurrency;
    }

    @Override
    public void crawl(URI start) {
        var normalizedStart = UriNormalizer.normalize(start);
        var scope    = new CrawlScope(normalizedStart.getHost());
        var depths   = new ConcurrentHashMap<URI, Integer>();
        var inFlight = new AtomicInteger(0);
        var throttle = new Semaphore(maxConcurrency);

        depths.put(normalizedStart, 0);
        frontier.add(normalizedStart);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            while (!frontier.isEmpty() || inFlight.get() > 0) {
                var uri = frontier.poll().orElse(null);
                if (uri == null) {
                    Thread.onSpinWait();
                    continue;
                }

                inFlight.incrementAndGet();
                throttle.acquireUninterruptibly();

                CompletableFuture
                    .supplyAsync(() -> fetchAndExtract(uri, scope), executor)
                    .thenAccept(links -> {
                        int depth    = depths.getOrDefault(uri, 0);
                        var newLinks = links.stream()
                                           .filter(link -> depths.putIfAbsent(link, depth + 1) == null)
                                           .collect(Collectors.toUnmodifiableSet());
                        reporter.report(uri, newLinks, depth);
                        newLinks.forEach(frontier::add);
                    })
                    .whenComplete((ignored, ex) -> {
                        if (ex != null) {
                            LOG.warning("Failed: %s — %s".formatted(uri, rootCause(ex)));
                        }
                        throttle.release();
                        inFlight.decrementAndGet();
                    });
            }
        }

        reporter.summarize();
    }

    private Set<URI> fetchAndExtract(URI uri, CrawlScope scope) {
        var html = fetcher.fetch(uri);
        return extractor.extract(uri, html).stream()
                        .filter(scope::isInScope)
                        .map(UriNormalizer::normalize)
                        .collect(Collectors.toUnmodifiableSet());
    }

    private static String rootCause(Throwable t) {
        var cause = t;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause.getMessage();
    }
}
