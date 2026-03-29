package com.webcrawler.infra.adapter.out.reporter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webcrawler.fixtures.UriFixtures;

public class ConsoleResultReporterTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    private final ConsoleResultReporter reporter = new ConsoleResultReporter();

    @BeforeEach
    void captureStdout() {
        System.setOut(new PrintStream(out));
    }

    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    @Test
    void shouldPrintVisitedUrlAndLinks() {
        reporter.report(UriFixtures.PAGE, Set.of(UriFixtures.PAGE_A, UriFixtures.PAGE_B), 0);

        var output = out.toString();
        assertTrue(output.contains("[VISITED] " + UriFixtures.PAGE));
        assertTrue(output.contains(UriFixtures.PAGE_A.toString()) || output.contains(UriFixtures.PAGE_B.toString()));
    }

    @Test
    void shouldPrintNoLinksMessageWhenFoundLinksIsEmpty() {
        reporter.report(UriFixtures.PAGE, Set.of(), 0);

        assertTrue(out.toString().contains("no links"));
    }
}
