package com.webcrawler.fixtures;

import java.net.URI;

public final class UriFixtures {
    private UriFixtures() {}

    public static final String MONZO = "https://crawlme.monzo.com/";
    public static final String MONZO_ABOUT = "https://crawlme.monzo.com/about";
    public static final String MONZO_BLOG = "https://crawlme.monzo.com/blog";

    public static final URI MONZO_ROOT_URI = URI.create(MONZO);
    public static final URI MONZO_ABOUT_URI = URI.create(MONZO_ABOUT);
    public static final URI MONZO_BLOG_URI = URI.create(MONZO_BLOG);

    public static final URI PAGE = URI.create("https://crawlme.monzo.com/");
    public static final URI PAGE_A = URI.create("https://crawlme.monzo.com/a");
    public static final URI PAGE_B = URI.create("https://crawlme.monzo.com/b");
    public static final URI PAGE_C = URI.create("https://crawlme.monzo.com/c");
    public static final URI EXTERNAL_PAGE = URI.create("https://external.com/");
}   
