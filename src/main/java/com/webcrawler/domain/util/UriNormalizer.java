package com.webcrawler.domain.util;

import java.net.URI;
import java.net.URISyntaxException;

public final class UriNormalizer {
    
    private UriNormalizer() {}

    private static URI rebuild(String scheme, String host, int port,
                                String path, String query, String fragment) {
        try {
            return new URI(scheme, null, host, port, path, query, fragment);
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException("Failed to rebuild URI: " + e.getMessage(), e);
        }
    }

    private static URI stripFragment(URI uri) {
        return rebuild(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
    }

    private static URI stripTrailingSlash(URI uri) {
        var path = uri.getPath();
        if (!path.endsWith("/") || path.length() == 1) {
            return uri;
        }
        return rebuild(uri.getScheme(), uri.getHost(), uri.getPort(),
                    path.substring(0, path.length() - 1),
                    uri.getQuery(), uri.getFragment());
    }

    private static URI stripDefaultPort(URI uri) {
        String scheme = uri.getScheme();
        int port = uri.getPort();
        boolean isDefaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                             || ("https".equalsIgnoreCase(scheme) && port == 443);
        if (!isDefaultPort) {
            return uri;
        } 
        return rebuild(scheme, uri.getHost(), 
                        -1, uri.getPath(), 
                        uri.getQuery(), uri.getFragment());
    }

    private static URI lowercaseHostAndScheme(URI uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String loweredScheme = scheme.toLowerCase();
        String loweredHost = host.toLowerCase();
        
        return rebuild(loweredScheme, loweredHost, 
                        uri.getPort(), uri.getPath(), 
                        uri.getQuery(), uri.getFragment());
    }
    
    private static URI stripQuery(URI uri) {
        return rebuild(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath(), null, uri.getFragment());
    }

    public static URI normalize(URI uri) {
        uri = stripFragment(uri);
        uri = stripQuery(uri);
        uri = stripTrailingSlash(uri);
        uri = stripDefaultPort(uri);
        return lowercaseHostAndScheme(uri);
    }
    
}
