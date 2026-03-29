package com.webcrawler.infra.adapter.out.parser;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.util.Set;
import com.webcrawler.domain.port.out.LinkExtractor;
import com.webcrawler.fixtures.HtmlPages;
import com.webcrawler.fixtures.Uris;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsoupLinkExtractorTest {
    private final LinkExtractor extractor = new JsoupLinkExtractor();

    @Test
    void shouldExtractAbsoluteLink() {
        var links = extractor.extract(Uris.MONZO_ROOT, HtmlPages.htmlWithLinks(Uris.MONZO_ABOUT.toString()));

        assertEquals(Set.of(Uris.MONZO_ABOUT), links);
    }

    @Test
    void shouldExtractRelativeLink() {
        var links = extractor.extract(Uris.MONZO_ROOT, HtmlPages.htmlWithLinks("about"));

        assertEquals(Set.of(Uris.MONZO_ABOUT), links);
    }

    @Test
    void shouldExtractExternalLink() {
        var external = URI.create("https://www.community.crawlme.monzo.com/about");

        var links = extractor.extract(Uris.MONZO_ROOT, HtmlPages.htmlWithLinks(external.toString()));

        assertEquals(Set.of(external), links);
    }

    @Test
    void shoudlDeduplicateLinks() {
        var html = HtmlPages.htmlWithLinks(Uris.MONZO_ABOUT.toString(), Uris.MONZO_ABOUT.toString());

        var links = extractor.extract(Uris.MONZO_ROOT, html);

        assertEquals(1, links.size());
    }

    @Test
    void shouldReturnEmptySetWhenNoLinks() {
        var links = extractor.extract(Uris.MONZO_ROOT, HtmlPages.htmlWithLinks());

        assertEquals(Set.of(), links);
    }
}
