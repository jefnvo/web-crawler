package com.webcrawler;

public record CrawlArgs(String url, boolean concurrent, int maxConcurrentRequests) {

    public static final int DEFAULT_MAX_CONCURRENCY = 1000;

    public static CrawlArgs parse(String[] args) {
        int threads;
        
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Usage: web-crawler <url> [faster [<threads>]]");
        }

        var url = args[0];

        if (args.length == 1) {
            return new CrawlArgs(url, false, DEFAULT_MAX_CONCURRENCY);
        }

        if (!args[1].equals("faster")) {
            throw new IllegalArgumentException("Unknown mode '%s'. Valid modes: faster".formatted(args[1]));
        }

        if (args.length == 2) {
            return new CrawlArgs(url, true, DEFAULT_MAX_CONCURRENCY);
        }

        try {
            threads = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Thread count must be a number, got: " + args[2]);
        }

        if (threads <= 0) {
            throw new IllegalArgumentException("Thread count must be positive, got: " + threads);
        }

        return new CrawlArgs(url, true, threads);
    }
}
