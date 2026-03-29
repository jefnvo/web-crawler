package com.webcrawler;

import java.util.ArrayList;
import java.util.List;

public record CrawlArgs(String url, boolean concurrent, int maxConcurrentRequests, int maxPages) {

    public static final int DEFAULT_MAX_CONCURRENCY = 1000;
    public static final int DEFAULT_MAX_PAGES = Integer.MAX_VALUE;

    private static final String FLAG_THREADS = "--threads";
    private static final String FLAG_MAX_PAGES = "--max-pages";

    public static CrawlArgs parse(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Usage: web-crawler <url> [faster [--threads <n>]] [--max-pages <n>]");
        }

        int maxPages = extractIntFlag(args, FLAG_MAX_PAGES, DEFAULT_MAX_PAGES);
        String[] positional = stripFlag(args, FLAG_MAX_PAGES);

        var url = positional[0];

        if (positional.length == 1) {
            return new CrawlArgs(url, false, DEFAULT_MAX_CONCURRENCY, maxPages);
        }

        if (!positional[1].equals("faster")) {
            throw new IllegalArgumentException("Unknown mode '%s'. Valid modes: faster".formatted(positional[1]));
        }

        int threads = extractIntFlag(positional, FLAG_THREADS, DEFAULT_MAX_CONCURRENCY);

        return new CrawlArgs(url, true, threads, maxPages);
    }

    private static int extractIntFlag(String[] args, String flag, int defaultValue) {
        for (int i = 0; i < args.length; i++) {
            if (flag.equals(args[i])) {
                if (i == args.length - 1) {
                    throw new IllegalArgumentException(flag + " requires a value");
                }
                try {
                    int n = Integer.parseInt(args[i + 1]);
                    if (n <= 0) {
                        throw new IllegalArgumentException(flag + " must be positive, got: " + n);
                    }
                    return n;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(flag + " must be a number, got: " + args[i + 1]);
                }
            }
        }
        return defaultValue;
    }

    private static String[] stripFlag(String[] args, String flag) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (flag.equals(args[i])) {
                i++;
            } else {
                result.add(args[i]);
            }
        }
        return result.toArray(String[]::new);
    }
}
