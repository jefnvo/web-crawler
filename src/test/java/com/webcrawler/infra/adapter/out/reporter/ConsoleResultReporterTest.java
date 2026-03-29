package com.webcrawler.infra.adapter.out.reporter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webcrawler.fixtures.Uris;

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
        reporter.report(Uris.PAGE, Set.of(Uris.PAGE_A, Uris.PAGE_B), 0);

        var output = out.toString();
        assertTrue(output.contains("[VISITED] " + Uris.PAGE));
        assertTrue(output.contains(Uris.PAGE_A.toString()) || output.contains(Uris.PAGE_B.toString()));
    }

    @Test
    void shouldPrintNoLinksMessageWhenFoundLinksIsEmpty() {
        reporter.report(Uris.PAGE, Set.of(), 0);

        assertTrue(out.toString().contains("no links"));
    }
}
