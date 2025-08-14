package org.mamallc.crawler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.mamallc.schemas.URL;
import org.mamallc.utils.API;
import org.mamallc.utils.URLString;

public class Crawler {

    public static Set<String> urlCache = new HashSet<>();

    public boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }

    public void crawlPage(String url, boolean writeToDB, boolean debug) {
        for (int i = 0; i < 50; i++)
            System.out.print("-");
        System.out.println();

        String content = null;
        String rootURL = URLString.getRootURL(url);
        java.net.URL conn = null;
        HttpURLConnection httpConn = null;

        // Navigate to the URL and wait for the page to load
        System.out.println("Navigating to " + url);
        long startTime = System.currentTimeMillis();

        try {
            if (debug)
                System.out.println("Fetching page content...");
            conn = new URI(url).toURL();
            httpConn = (HttpURLConnection) conn.openConnection();
            httpConn.setConnectTimeout(8000);
            httpConn.setReadTimeout(5000);
            httpConn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line);
            }
            content = contentBuilder.toString();
            reader.close();
            if (debug)
                System.out.println("Page fetched successfully.");
        } catch (SocketTimeoutException e) {
            System.out.println("Connection timed out while fetching the page: " + url);
            return;
        } catch (Exception e) {
            System.out.println("Failed to fetch the page: " + e.getMessage());
            ;
            return;
        }

        // Parsing the HTML for words
        boolean withinAnchor = false;
        boolean withinAngledBraces = false;
        boolean withinTag = false;
        boolean withinURL = false;
        boolean withinTitle = false;
        boolean withinBody = false;
        boolean withinScript = false;
        boolean withinStyle = false;
        boolean withinMeta = false;

        StringBuilder nestedURL = new StringBuilder();
        StringBuilder title = new StringBuilder();
        StringBuilder sentence = new StringBuilder();
        StringBuilder tag = new StringBuilder();
        StringBuilder anchorText = new StringBuilder();
        StringBuilder metaAttributes = new StringBuilder();
        String metaDescription = "";
        String metaKeywords = "";

        List<String> textList = new ArrayList<>();
        Set<URL> queue = new HashSet<>();
        Set<String> queueOfStrings = new HashSet<>();

        if (content != null && !content.isEmpty() && !content.isBlank()) {
            try {
                for (int stringPointer = 0; stringPointer < content.length(); stringPointer++) {
                    // Reaching end of opening tag
                    // Either <tag> or <tag attr="kadfl" ...
                    if ((content.charAt(stringPointer) == ' ' || content.charAt(stringPointer) == '>')
                            && withinAngledBraces) {
                        if (content.charAt(stringPointer) == '>') {
                            withinAngledBraces = false;
                            if (tag.toString().isEmpty() && withinMeta) {
                                withinMeta = false;
                                String[] metaArr = metaAttributes.toString().split("\" ");
                                String metaName = "";
                                for (String attr : metaArr) {
                                    String[] attrArr = attr.split("=");
                                    String key = "", value = "";
                                    if (attrArr.length > 1) {
                                        key = attrArr[0].trim();
                                        value = attrArr[1].trim().replace("\"", "");
                                    }
                                    if (key.equals("name") && value.equals("description")) {
                                        metaName = "description";
                                    }
                                    if (key.equals("name") && value.equals("keywords")) {
                                        System.out.println("Found keywords");
                                        metaName = "keywords";
                                    }
                                    if (key.equals("content") && metaName.equals("description")) {
                                        metaDescription = value;
                                        metaName = "";
                                    }
                                    if (key.equals("content") && metaName.equals("keywords")) {
                                        metaKeywords = value;
                                        metaName = "";
                                    }
                                }
                                metaAttributes = new StringBuilder();
                            }
                        }
                        if (withinMeta && content.charAt(stringPointer) == ' ') {
                            metaAttributes.append(' ');
                        }
                        if (withinTag) {
                            String tagString = tag.toString().strip();
                            withinTag = false;
                            // We need to know which tag we are about to enter or leave
                            if (tagString.equals("script"))
                                withinScript = true;
                            if (tagString.equals("/script") && withinScript)
                                withinScript = false;
                            if (tagString.equals("meta"))
                                withinMeta = true;
                            if (tagString.equals("style"))
                                withinStyle = true;
                            if (tagString.equals("/style") && withinStyle)
                                withinStyle = false;
                            if (tagString.equals("title") && !withinTitle)
                                withinTitle = true;
                            if (!withinBody && tagString.equals("body"))
                                withinBody = true;
                            if (withinBody && tagString.equals("/body"))
                                withinBody = false;
                            if (tagString.equals("a"))
                                withinAnchor = true;
                            if (tagString.equals("/a")) {
                                withinAnchor = false;
                                String nestedURLString = URLString.removeUTFStrings(nestedURL.toString());
                                if (!nestedURLString.isEmpty() && !nestedURLString.isBlank()
                                        && !queueOfStrings.contains(nestedURLString)
                                        && !nestedURLString.equalsIgnoreCase("javascript:void(0);")) {
                                    if (debug)
                                        System.out.println("Initial String: " + nestedURLString);
                                    String finalString = URLString.processURL(nestedURLString, url, rootURL);
                                    // Replace possible UTF-8 characters with symbols
                                    finalString = URLString.removeUTFStrings(finalString);
                                    if (debug)
                                        System.out.println("Final string: " + finalString);

                                    URL queueURL = new URL(finalString);
                                    String anchorTextString = anchorText.toString().trim().strip();
                                    queueURL.setAnchorText(anchorTextString);

                                    if (!urlCache.contains(finalString))
                                        queue.add(queueURL);
                                    urlCache.add(finalString);
                                    queueOfStrings.add(finalString);
                                    nestedURL = new StringBuilder();
                                    anchorText = new StringBuilder();
                                }
                            }
                            tag = new StringBuilder();
                        }
                        continue;
                    }
                    if (withinURL && content.charAt(stringPointer) == '"') {
                        withinURL = false;
                    }

                    if (content.charAt(stringPointer) == '<' && !withinAngledBraces) {
                        withinAngledBraces = true;
                        withinTag = true;
                        if (withinTitle)
                            withinTitle = false;
                        if (withinAnchor)
                            anchorText.append(" ");
                        String sentenceString = sentence.toString().trim();
                        if (!sentenceString.isEmpty() || !sentenceString.isBlank())
                            textList.add(sentenceString);
                        sentence = new StringBuilder();
                        continue;
                    }

                    if (withinURL) {
                        nestedURL.append(content.charAt(stringPointer));
                    }

                    if (withinTitle && !withinBody && !withinAngledBraces) {
                        title.append(content.charAt(stringPointer));
                    }

                    if (!withinAngledBraces && withinBody && !withinScript && !withinStyle) {
                        sentence.append(content.charAt(stringPointer));
                        if (withinAnchor)
                            anchorText.append(content.charAt(stringPointer));
                    }

                    // Checking to see if we found the href attr in the anchor
                    if (withinAnchor && !withinURL && content.charAt(stringPointer) == 'h'
                            && content.charAt(stringPointer + 1) == 'r' && content.charAt(stringPointer + 2) == 'e'
                            && content.charAt(stringPointer + 3) == 'f') {
                        nestedURL = new StringBuilder();
                        while (content.charAt(stringPointer) != '"')
                            stringPointer++;
                        withinURL = true;
                    }

                    if (withinTag)
                        tag.append(content.charAt(stringPointer));

                    if (withinAngledBraces && withinMeta)
                        metaAttributes.append(content.charAt(stringPointer));
                }
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                System.out.println("Crawled " + url + " in " + duration / 1000.0 + " s");

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (writeToDB)
                API.insertCrawlEntry(queue, queueOfStrings, url, textList, title.toString().trim(), metaDescription,
                        metaKeywords);
        }
    }

    public void crawl(Map<String, String> config) throws InterruptedException {
        int threads = Integer.parseInt(config.get("--threads"));
        boolean writeToDB = Boolean.parseBoolean(config.get("--write-db"));
        boolean debug = Boolean.parseBoolean(config.get("--debug"));
        if (writeToDB)
            System.out.println("Writing to DB is enabled");
        else
            System.out.println("Writing to DB is disabled");
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();
        System.out.println("Using " + threads + " threads, crawling will start in 5 seconds...");
        TimeUnit.SECONDS.sleep(5);

        while (true) {
            String[] urls = API.getNextURL();

            if (urlCache.size() >= 50000) {
                System.out.println("URL cache size exceeded 10,000, clearing cache...");
                urlCache.clear();
            }

            if (urls.length > 0)
                for (String url : urls) {
                    Future<?> future = executor.submit(() -> {
                        crawlPage(url, writeToDB, debug);
                    });
                    futures.add(future);
                }
            if (futures.size() >= 10) {
                Iterator<Future<?>> iterator = futures.iterator();
                while (iterator.hasNext()) {
                    Future<?> future = iterator.next();
                    try {
                        future.get();
                        iterator.remove();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
