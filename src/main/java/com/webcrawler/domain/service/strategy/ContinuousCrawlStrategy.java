package com.webcrawler.domain.service.strategy;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.webcrawler.domain.port.out.ResultReporter;
import com.webcrawler.domain.service.PageProcessor;
import com.webcrawler.domain.util.CrawlScope;
import com.webcrawler.domain.util.UriNormalizer;

/*
 * Implements a continuous crawl strategy using a bounded thread pool with back-pressure.
 * Unlike the wave-based ConcurrentCrawlStrategy, there is no synchronization barrier between
 * levels — each task immediately submits newly discovered links as new tasks.
 *
 * Back-pressure is achieved via a bounded LinkedBlockingQueue with CallerRunsPolicy:
 * when the queue is full, the submitting thread runs the task inline, naturally throttling
 * the submission rate and preventing unbounded memory growth.
 *
 * Termination is detected via an AtomicInteger (activeTasks): incremented before submit,
 * decremented after all children are submitted. It can only reach zero when the entire
 * reachable graph has been processed.
 */
public class ContinuousCrawlStrategy implements CrawlStrategy {

    private static final Logger LOG = Logger.getLogger(ContinuousCrawlStrategy.class.getName());

    private static final int QUEUE_DEPTH_MULTIPLIER = 10;

    private final PageProcessor  processor;
    private final ResultReporter reporter;
    private final int            maxConcurrency;
    private final int            maxPages;

    public ContinuousCrawlStrategy(PageProcessor processor, ResultReporter reporter,
                                   int maxConcurrency, int maxPages) {
        this.processor      = processor;
        this.reporter       = reporter;
        this.maxConcurrency = maxConcurrency;
        this.maxPages       = maxPages;
    }

    private record CrawlContext(
        ThreadPoolExecutor  executor,
        Set<URI>            visited,
        CrawlScope          scope,
        AtomicInteger       pagesVisited,
        AtomicInteger       activeTasks,
        CompletableFuture<Void> done
    ) {}

    @Override
    public void crawl(URI start) {
        var normalized = UriNormalizer.normalize(start);
        var queue      = new LinkedBlockingQueue<Runnable>(maxConcurrency * QUEUE_DEPTH_MULTIPLIER);
        var executor   = new ThreadPoolExecutor(
            maxConcurrency, maxConcurrency,
            0L, TimeUnit.MILLISECONDS,
            queue,
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        var ctx = new CrawlContext(
            executor,
            ConcurrentHashMap.newKeySet(),
            new CrawlScope(normalized.getHost()),
            new AtomicInteger(0),
            new AtomicInteger(0),
            new CompletableFuture<>()
        );

        ctx.visited().add(normalized);
        submit(normalized, 0, ctx);
        ctx.done().join();
        executor.shutdown();

        reporter.summarize();
    }

    private void submit(URI uri, int depth, CrawlContext ctx) {
        ctx.activeTasks().incrementAndGet();
        ctx.executor().submit(() -> {
            try {
                var newLinks = processor.fetchLinks(uri, ctx.scope()).stream()
                    .filter(link -> ctx.visited().add(link))
                    .collect(Collectors.toUnmodifiableSet());

                reporter.report(uri, newLinks, depth);

                if (ctx.pagesVisited().incrementAndGet() < maxPages) {
                    newLinks.forEach(link -> submit(link, depth + 1, ctx));
                }
            } catch (Exception ex) {
                LOG.warning("Failed: %s — %s".formatted(uri, ex.getMessage()));
            } finally {
                // decrement after submitting children — invariant that prevents false zero
                if (ctx.activeTasks().decrementAndGet() == 0) {
                    ctx.done().complete(null);
                }
            }
        });
    }

}