package com.webcrawler.fixtures;

public final class HtmlPages {
    public static final String MINIMAL = "<html/>";

    public static String htmlWithLinks(String... hrefs) {
        var sb = new StringBuilder("<html><body>");
        for (var href : hrefs) {
            sb.append("<a href=\"").append(href).append("\">link</a>");
        }
        return sb.append("</body></html>").toString();
    }


}
