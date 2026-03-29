package com.webcrawler.infra.adapter.out.parser;

import java.util.Set;
import java.util.stream.Collectors;
import java.net.URI;
import org.jsoup.Jsoup;
import com.webcrawler.domain.port.out.LinkExtractor;


public class JsoupLinkExtractor implements LinkExtractor {

    @Override
    public Set<URI> extract(URI pageUri, String html) {
        return Jsoup.parse(html, pageUri.toString())
               .select("a[href]")
               .stream()
               .map(a -> a.absUrl("href"))
               .filter(url -> !url.isBlank())
               .map(URI::create)
               .collect(Collectors.toSet());
    }
}
