package com.webcrawler.domain.service.strategy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.webcrawler.domain.port.out.ResultReporter;
import com.webcrawler.domain.service.PageProcessor;
import com.webcrawler.domain.service.frontier.Frontier;
import com.webcrawler.domain.util.CrawlScope;
import com.webcrawler.domain.util.UriNormalizer;

/*
* Implements a concurrent crawl strategy using a thread-per-task executor.
* The frontier is shared across threads, so it must be thread-safe (e.g., ConcurrentBfsFrontier).
* The parallelism here is wave-based, we execute a level in parallel but wait for the level finish and synchronize before moving to the next level.
* This allows us to maintain a clear depth structure for reporting and scope management, while still achieving concurrency within each level.
* You can swap the frontier implementation to change the traversal order without affecting the concurrency model. (e.g., use a concurrent DFS frontier for depth-first traversal)
*/
public class ConcurrentCrawlStrategy implements CrawlStrategy {

    private static final Logger LOG = Logger.getLogger(ConcurrentCrawlStrategy.class.getName());

    private final PageProcessor processor;
    private final ResultReporter reporter;
    private final int maxConcurrency;
    private final int maxPages;
    private final Supplier<Frontier> frontierFactory;

    public ConcurrentCrawlStrategy(PageProcessor processor, ResultReporter reporter, int maxConcurrency, int maxPages, Supplier<Frontier> frontierFactory) {
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
        var throttle = new Semaphore(maxConcurrency);
        frontier.offer(normalized, 0);

        int pagesVisited = 0;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            while (!frontier.isEmpty() && pagesVisited < maxPages) {
                List<Callable<Void>> wave = drainWave(frontier, scope, throttle);
                executor.invokeAll(wave);
                pagesVisited += wave.size();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        reporter.summarize();
    }

    private List<Callable<Void>> drainWave(Frontier frontier, CrawlScope scope, Semaphore throttle) {
        List<Callable<Void>> tasks = new ArrayList<>();
        URI uri;
        while ((uri = frontier.pickWork()) != null) {
            var target = uri;
            tasks.add(() -> { processPage(target, frontier, scope, throttle); return null; });
        }
        return tasks;
    }

    private void processPage(URI uri, Frontier frontier, CrawlScope scope, Semaphore throttle) {
        int depth = frontier.depthOf(uri);
        throttle.acquireUninterruptibly();
        try {
            var newLinks = processor.fetchLinks(uri, scope).stream()
                .filter(link -> frontier.offer(link, depth + 1))
                .collect(Collectors.toUnmodifiableSet());
            reporter.report(uri, newLinks, depth);
        } catch (Exception ex) {
            LOG.warning("Failed: %s — %s".formatted(uri, ex.getMessage()));
        } finally {
            throttle.release();
        }
    }
}
