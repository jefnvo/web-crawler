package com.webcrawler.domain.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;
import org.junit.jupiter.api.Test;
import com.webcrawler.fixtures.UriFixtures;

public class UriNormalizerTest {
    @Test
    void shouldStripFragment() {
        var uri = URI.create(UriFixtures.MONZO_ABOUT + "#section");
        assertEquals(UriFixtures.MONZO_ABOUT_URI,
                    UriNormalizer.normalize(uri));
    }

    @Test
    void shouldStripTrailingSlash() {
        var uri = URI.create(UriFixtures.MONZO_ABOUT + "/");
        assertEquals(URI.create(UriFixtures.MONZO_ABOUT),
                    UriNormalizer.normalize(uri));
    }

    @Test
    void shouldKeepRootPathWhenNoTrailingSlashToStrip() {
        var uri = URI.create(UriFixtures.MONZO + "/");
        assertEquals(
            UriFixtures.MONZO_ROOT_URI,
            UriNormalizer.normalize(uri)
        );
    }

    @Test
    void shouldStripFragmentAndTrailingSlash() {
        var uri = URI.create(UriFixtures.MONZO_ABOUT + "/#section");
        assertEquals(UriFixtures.MONZO_ABOUT_URI,
                    UriNormalizer.normalize(uri));
    }

    @Test
    void shouldStripDefaultPort() {
        var uri = URI.create("http://crawlme.monzo.com:80/page");
        assertEquals(URI.create("http://crawlme.monzo.com/page"),
                    UriNormalizer.normalize(uri));
    }

    @Test
    void shouldLowercaseSchemeAndHost() {
        var uri = URI.create("HTTPS://CrawlMe.Monzo.Com/About");
        assertEquals(URI.create("https://crawlme.monzo.com/About"),
                    UriNormalizer.normalize(uri));
    }

}
