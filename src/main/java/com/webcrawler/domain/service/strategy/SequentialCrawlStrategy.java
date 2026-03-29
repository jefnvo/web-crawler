package com.webcrawler.domain.service.strategy;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.webcrawler.domain.port.out.LinkExtractor;
import com.webcrawler.domain.port.out.PageFetchException;
import com.webcrawler.domain.port.out.PageFetcher;
import com.webcrawler.domain.port.out.ResultReporter;
import com.webcrawler.domain.service.frontier.Frontier;
import com.webcrawler.domain.util.CrawlScope;
import com.webcrawler.domain.util.UriNormalizer;

/**
 * Traversal order is determined by the injected Frontier
 * swap BfsFrontier for a DfsFrontier or PriorityFrontier.
 */
public class SequentialCrawlStrategy implements CrawlStrategy {

    private static final Logger LOG = Logger.getLogger(SequentialCrawlStrategy.class.getName());

    private final PageFetcher fetcher;
    private final LinkExtractor extractor;
    private final ResultReporter reporter;
    private final Frontier frontier;

    public SequentialCrawlStrategy(PageFetcher fetcher, LinkExtractor extractor,
                                   ResultReporter reporter, Frontier frontier) {
        this.fetcher   = fetcher;
        this.extractor = extractor;
        this.reporter  = reporter;
        this.frontier  = frontier;
    }

    @Override
    public void crawl(URI start) {
        var normalizedStart = UriNormalizer.normalize(start);
        var scope           = new CrawlScope(normalizedStart.getHost());
        Map<URI, Integer> depths = new HashMap<>();

        depths.put(normalizedStart, 0);
        frontier.add(normalizedStart);

        while (!frontier.isEmpty()) {
            frontier.poll().ifPresent(uri -> {
                try {
                    processPage(uri, scope, depths);
                } catch (PageFetchException e) {
                    LOG.warning("Failed to fetch: " + uri + " — " + e.getMessage());
                }
            });
        }

        reporter.summarize();
    }

    private void processPage(URI uri, CrawlScope scope, Map<URI, Integer> depths) {
        int depth    = depths.getOrDefault(uri, 0);
        var html     = fetcher.fetch(uri);
        var newLinks = extractor.extract(uri, html).stream()
                                .filter(scope::isInScope)
                                .map(UriNormalizer::normalize)
                                .filter(link -> depths.putIfAbsent(link, depth + 1) == null)
                                .collect(Collectors.toUnmodifiableSet());

        reporter.report(uri, newLinks, depth);
        newLinks.forEach(frontier::add);
    }
}
