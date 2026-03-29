package com.webcrawler.infra.adapter.out.reporter;

import com.webcrawler.domain.port.out.ResultReporter;

import java.net.URI;
import java.util.Set;

public class ConsoleResultReporter implements ResultReporter {

    @Override
    public void report(URI visited, Set<URI> foundLinks, int depth) {
        System.out.println("[VISITED] " + visited);
        if (!foundLinks.isEmpty()) {
            foundLinks.forEach(link -> System.out.println("  -> " + link));
        } else {
            System.out.println("  This page has no links or all links have already been visited.");
        }
        System.out.println();
    }
}
