package com.webcrawler;

import com.webcrawler.domain.port.in.CrawlUseCase;
import com.webcrawler.domain.service.CrawlController;
import com.webcrawler.domain.service.frontier.BfsFrontier;
import com.webcrawler.domain.service.frontier.ConcurrentBfsFrontier;
import com.webcrawler.domain.service.strategy.ConcurrentCrawlStrategy;
import com.webcrawler.domain.service.strategy.CrawlStrategy;
import com.webcrawler.domain.service.strategy.SequentialCrawlStrategy;
import com.webcrawler.infra.adapter.out.http.HttpPageFetcher;
import com.webcrawler.infra.adapter.out.parser.JsoupLinkExtractor;
import com.webcrawler.infra.adapter.out.reporter.ConsoleResultReporter;
import com.webcrawler.infra.adapter.out.reporter.InstrumentedReporter;
import com.webcrawler.infra.config.HttpClientConfig;

public class App {
    public static void main(String[] args) {
        CrawlArgs crawlArgs;
        try {
            crawlArgs = CrawlArgs.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
            return;
        }

        var config    = HttpClientConfig.defaults();
        var fetcher   = new HttpPageFetcher(config);
        var extractor = new JsoupLinkExtractor();
        var reporter  = new InstrumentedReporter(new ConsoleResultReporter());

        CrawlStrategy strategy = crawlArgs.concurrent()
            ? new ConcurrentCrawlStrategy(fetcher, extractor, reporter,
                                          new ConcurrentBfsFrontier(),
                                          crawlArgs.maxConcurrentRequests())
            : new SequentialCrawlStrategy(fetcher, extractor, reporter, new BfsFrontier());

        CrawlUseCase crawler = new CrawlController(strategy);
        crawler.crawl(crawlArgs.url());
    }
}
