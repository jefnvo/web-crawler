package com.webcrawler.domain.service.strategy;

import java.net.URI;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.webcrawler.domain.port.out.PageFetchException;
import com.webcrawler.domain.port.out.ResultReporter;
import com.webcrawler.domain.service.PageProcessor;
import com.webcrawler.domain.service.frontier.BfsFrontier;
import com.webcrawler.domain.service.frontier.Frontier;
import com.webcrawler.domain.util.CrawlScope;
import com.webcrawler.domain.util.UriNormalizer;

public class SequentialCrawlStrategy implements CrawlStrategy {

    private static final Logger LOG = Logger.getLogger(SequentialCrawlStrategy.class.getName());

    private final PageProcessor processor;
    private final ResultReporter reporter;
    private final Supplier<Frontier> frontierFactory;

    public SequentialCrawlStrategy(PageProcessor processor, ResultReporter reporter) {
        this(processor, reporter, BfsFrontier::new);
    }

    public SequentialCrawlStrategy(PageProcessor processor, ResultReporter reporter, Supplier<Frontier> frontierFactory) {
        this.processor = processor;
        this.reporter = reporter;
        this.frontierFactory = frontierFactory;
    }

    @Override
    public void crawl(URI start) {
        var normalized = UriNormalizer.normalize(start);
        var scope = new CrawlScope(normalized.getHost());
        var frontier = frontierFactory.get();
        frontier.offer(normalized, 0);

        while (!frontier.isEmpty()) {
            var uri = frontier.poll();
            try {
                processPage(uri, scope, frontier);
            } catch (PageFetchException e) {
                LOG.warning("Failed to fetch: " + uri + " — " + e.getMessage());
            }
        }

        reporter.summarize();
    }

    private void processPage(URI uri, CrawlScope scope, Frontier frontier) {
        int depth = frontier.depthOf(uri);
        var newLinks = processor.fetchLinks(uri, scope).stream()
                                .filter(link -> frontier.offer(link, depth + 1))
                                .collect(Collectors.toUnmodifiableSet());
        reporter.report(uri, newLinks, depth);
    }
}
