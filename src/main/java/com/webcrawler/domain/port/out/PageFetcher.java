package com.webcrawler.domain.port.out;

import java.net.URI;

public interface PageFetcher {
    String fetch(URI uri);
}
