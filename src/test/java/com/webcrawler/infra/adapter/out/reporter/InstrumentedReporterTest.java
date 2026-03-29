package com.webcrawler.infra.adapter.out.reporter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webcrawler.domain.port.out.ResultReporter;
import com.webcrawler.fixtures.UriFixtures;

public class InstrumentedReporterTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    private final ResultReporter delegate = mock(ResultReporter.class);
    private final InstrumentedReporter reporter = new InstrumentedReporter(delegate);

    @BeforeEach
    void captureStdout() {
        System.setOut(new PrintStream(out));
    }

    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    @Test
    void shouldDelegateReportToInnerReporter() {
        reporter.report(UriFixtures.PAGE, Set.of(UriFixtures.PAGE_A), 0);

        verify(delegate).report(UriFixtures.PAGE, Set.of(UriFixtures.PAGE_A), 0);
    }

    @Test
    void shouldDelegateSummarizeToInnerReporter() {
        reporter.summarize();

        verify(delegate).summarize();
    }

    @Test
    void shouldAccumulatePageCount() {
        reporter.report(UriFixtures.PAGE, Set.of(), 0);
        reporter.report(UriFixtures.PAGE_A,    Set.of(), 1);
        reporter.report(UriFixtures.PAGE_B,    Set.of(), 2);
        reporter.summarize();

        assertTrue(out.toString().contains("3"), "summary must show 3 pages visited");
    }

    @Test
    void shouldTrackMaxDepth() {
        reporter.report(UriFixtures.PAGE, Set.of(), 0);
        reporter.report(UriFixtures.PAGE_A,    Set.of(), 3);
        reporter.report(UriFixtures.PAGE_B,    Set.of(), 1);
        reporter.summarize();

        assertTrue(out.toString().contains("3"), "summary must show max depth of 3");
    }

    @Test
    void shouldPrintSummaryAfterDelegateSummarize() {
        reporter.report(UriFixtures.PAGE, Set.of(UriFixtures.PAGE_A), 0);
        reporter.report(UriFixtures.PAGE_A,    Set.of(), 1);
        reporter.summarize();

        var output = out.toString();
        assertTrue(output.contains("2"));
        assertTrue(output.contains("1"));
    }
}
