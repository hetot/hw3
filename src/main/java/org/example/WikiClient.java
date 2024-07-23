package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

public class WikiClient {

    public static final String WIKI = "/wiki/";
    public static final String EN_WIKI_URL = "https://en.wikipedia.org" + WIKI;

    public Map<String, List<String>> getByTitle(String title) throws IOException {
        Map<String, List<String>> linkMap = new HashMap<>();
        String url = EN_WIKI_URL + title;
        try {
            common(linkMap, url);
        } catch (Exception e) {
            System.out.println(title + " got error: " + e.getMessage());
        }
        return linkMap;
    }

    private void common(Map<String, List<String>> linkMap, String url) throws IOException {
        Document page = Jsoup.connect(url).timeout(20000).followRedirects(true).get();
        for (Element element : page.body().select("a")) {
            if (element.hasAttr("href")) {
                String href = element.attr("href");
                if (href.startsWith(WIKI) && !element.text().isEmpty() && !href.contains(":")) {
                    linkMap.put(href.substring(WIKI.length()), List.of(element.text(), element.attr("title")));
                }
            }
        }
    }
}

