package com.webcrawler.domain.port.out;

import java.net.URI;
import java.util.Set;


public interface LinkExtractor {
    Set<URI> extract (URI pageUri, String html);
}
