package com.webcrawler.infra.adapter.out.parser;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.util.Set;
import com.webcrawler.domain.port.out.LinkExtractor;
import com.webcrawler.fixtures.HtmlPages;
import com.webcrawler.fixtures.UriFixtures;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsoupLinkExtractorTest {
    private final LinkExtractor extractor = new JsoupLinkExtractor();

    @Test
    void shouldExtractAbsoluteLink() {
        var links = extractor.extract(UriFixtures.MONZO_ROOT_URI, HtmlPages.htmlWithLinks(UriFixtures.MONZO_ABOUT_URI.toString()));

        assertEquals(Set.of(UriFixtures.MONZO_ABOUT_URI), links);
    }

    @Test
    void shouldExtractRelativeLink() {
        var links = extractor.extract(UriFixtures.MONZO_ROOT_URI, HtmlPages.htmlWithLinks("about"));

        assertEquals(Set.of(UriFixtures.MONZO_ABOUT_URI), links);
    }

    @Test
    void shouldExtractExternalLink() {
        var external = URI.create("https://www.community.crawlme.monzo.com/about");

        var links = extractor.extract(UriFixtures.MONZO_ROOT_URI, HtmlPages.htmlWithLinks(external.toString()));

        assertEquals(Set.of(external), links);
    }

    @Test
    void shoudlDeduplicateLinks() {
        var html = HtmlPages.htmlWithLinks(UriFixtures.MONZO_ABOUT.toString(), UriFixtures.MONZO_ABOUT.toString());

        var links = extractor.extract(UriFixtures.MONZO_ROOT_URI, html);

        assertEquals(1, links.size());
    }

    @Test
    void shouldReturnEmptySetWhenNoLinks() {
        var links = extractor.extract(UriFixtures.MONZO_ROOT_URI, HtmlPages.htmlWithLinks());

        assertEquals(Set.of(), links);
    }
}
