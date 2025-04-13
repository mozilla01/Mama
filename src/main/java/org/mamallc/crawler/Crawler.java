package org.mamallc.crawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.mamallc.utils.API;
import org.mamallc.utils.URL;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Page;

public class Crawler {

    public static String decompress(String str) throws Exception {
        byte[] byteCompressed = str.getBytes(StandardCharsets.UTF_8);
        final StringBuilder outStr = new StringBuilder();
        if ((byteCompressed == null) || (byteCompressed.length == 0)) {
            return "";
        }
        if (isCompressed(byteCompressed)) {
            final GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(byteCompressed));
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outStr.append(line);
            }
        } else {
            outStr.append(byteCompressed);
        }
        return outStr.toString();
    }

    public static boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC))
                && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }

    public static String removeUTFStrings(String str) {
        return str.replace("&#x2F;", "/").replace("&#x3D;", "=").replace("&#x26;", "&")
                .replace("&#x3F;", "?").replace("&#x25;", "%").replace("&#x3A;", ":")
                .replace("&#x2C;", ",").replace("&#x2D;", "-").replace("&#x2E;", ".").replace("&#x23;", "#")
                .replace("&#x3B;", ";").replace("&#x3C;", "<").replace("&#x3E;", ">").replace("&#x40;", "@")
                .replace("&#x5F;", "_").replace("&#x7E;", "~").replace("&#x5B;", "[").replace("&#x5D;", "]")
                .replace("&#x7B;", "{").replace("&#x7D;", "}").replace("&#x7C;", "|").replace("&#x60;", "`")
                .replace("&#x22;", "\"").replace("&#x27;", "'").replace("&#x3E;", ">").replace("&#x3C;", "<")
                .replace("&#x5C;", "\\").replace("&#x24;", "$").replace("&#x40;", "@").replace("&#x2B;", "+")
                .replace("&#x3D;", "=").replace("&#x2A;", "*").replace("&#x25;", "%").replace("&#x5E;", "^")
                .replace("&#x21;", "!").replace("&#x3F;", "?").replace("&#x40;", "@").replace("&#x2C;", ",")
                .replace("&#x2E;", ".").replace("&#x2F;", "/").replace("&#x3A;", ":").replace("&#x3B;", ";")
                .replace("&#x3D;", "=").replace("&#x3F;", "?").replace("&#x40;", "@").replace("&#x5B;", "[")
                .replace("&#x5C;", "\\").replace("&#x5D;", "]").replace("&#x5E;", "^").replace("&#x5F;", "_")
                .replace("&#x60;", "`").replace("&#x7B;", "{").replace("&#x7C;", "|").replace("&#x7D;", "}")
                .replace("&#x7E;", "~");
    }

    String getRootURL(String url) {
        String rootURL = "https://example.com";
        try {
            int i = 0;
            while (url.charAt(i) != '.')
                i++;
            while (i < url.length() && url.charAt(i) != '/')
                i++;
            rootURL = url.substring(0, i);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return rootURL;
    }

    int checkForProtocol(String url) {
        StringBuilder protocol = new StringBuilder();
        url = url.trim();
        int valid = 0;
        int i = 0;
        while (i < url.length() && i < 4) {
            protocol.append(url.charAt(i));
            i++;
        }
        if (protocol.length() > 1 && protocol.charAt(0) == '/' && protocol.charAt(1) == '/')
            valid = 1; // Unusual URL, do not append
        else if ((protocol.length() > 1 && protocol.charAt(0) == '/' && protocol.charAt(1) != '/')
                || (protocol.length() == 1 && protocol.charAt(0) == '/'))
            valid = 2; // Simple nested route, append root
        else if (protocol.toString().equals("http"))
            valid = 3; // Regular URL, do not append
        else if (protocol.length() > 0 && (protocol.charAt(0) == '#' || protocol.charAt(0) == '?'))
            valid = 2; // Anchor or query string, do not append
        else
            valid = 4; // hide?p=1 something
        return valid;
    }

    public String processURL(String nestedURLString, String url, String rootURL) {
        String finalString = "";
        int valid = checkForProtocol(nestedURLString);
        if (valid == 0 || valid == 3) {
            finalString = nestedURLString;
        } else if (valid == 1) {
            finalString = "https:" + nestedURLString;
        } else if (valid == 2) {
            finalString = rootURL + nestedURLString;
        } else if (valid == 4) {
            finalString = rootURL + "/" + nestedURLString;
        }
        return finalString;
    }

    boolean checkRobotsTxt(String url, String rootURL, Scanner sc) {
        boolean canVisit = true;
        URLConnection conn = null;
        String currentRoot = getRootURL(url); // Root URL of the URL we are checking
        try {
            if (!currentRoot.equals(rootURL)) {
                conn = new URI(rootURL + "/robots.txt").toURL().openConnection();
                sc = new Scanner(conn.getInputStream());
                sc.useDelimiter("\n");
            }
            while (sc.hasNext()) {
                String line = sc.nextLine();
                String arr[] = line.split(":");
                if (arr.length > 1) {
                    if (arr[0].trim().equals("User-agent") && arr[1].trim().equals("*")) {
                        String rule[] = sc.nextLine().split(":");
                        while (rule.length < 2)
                            rule = sc.nextLine().split(":");
                        String directive = rule[0].trim();
                        String route = rule[1].trim();
                        while (rule[0].trim().equals("Disallow") || rule[0].trim().equals("Allow")) {
                            directive = rule[0].trim();
                            route = rule[1].trim();
                            Pattern regex = Pattern.compile(route);
                            Matcher matcher = regex.matcher(url);
                            if (directive.equals("Disallow") && matcher.find()) {
                                canVisit = false;
                            }
                            if (directive.equals("Allow") && matcher.find()) {
                                canVisit = true;
                            }
                            rule = sc.nextLine().split(":");
                        }
                    }
                }
            }
            sc.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return canVisit;
    }

    public boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }

    public void crawlPage(String url, Page page) {
        for (int i = 0; i < 50; i++)
            System.out.print("-");
        System.out.println();

        String content = null;
        String rootURL = getRootURL(url);

        // Navigate to the URL and wait for the page to load
        System.out.println("Navigating to " + url);
        try {
            page.navigate(url, new Page.NavigateOptions().setTimeout(8000));
            content = page.evaluate("() => document.documentElement.innerHTML").toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
            // try {
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
                            String nestedURLString = removeUTFStrings(nestedURL.toString());
                            if (!nestedURLString.isEmpty() && !nestedURLString.isBlank()
                                    && !queueOfStrings.contains(nestedURLString)
                                    && !nestedURLString.equalsIgnoreCase("javascript:void(0);")) {
                                System.out.println("Initial String: " + nestedURLString);
                                String finalString = processURL(nestedURLString, url, rootURL);
                                // Replace possible UTF-8 characters with symbols
                                finalString = removeUTFStrings(finalString);
                                System.out.println("Final string: " + finalString);

                                URL queueURL = new URL(finalString);
                                String anchorTextString = anchorText.toString().trim().strip();
                                queueURL.setAnchorText(anchorTextString);

                                queue.add(queueURL);
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
                    while (content.charAt(stringPointer) != '"')
                        stringPointer++;
                    withinURL = true;
                }

                if (withinTag)
                    tag.append(content.charAt(stringPointer));

                if (withinAngledBraces && withinMeta)
                    metaAttributes.append(content.charAt(stringPointer));
            }
            API.insertCrawlEntry(queue, queueOfStrings, url, textList, title.toString().trim(), metaDescription,
                    metaKeywords);
        }
    }

    public void fetchPage() {

        Playwright playwright = Playwright.create();
        boolean headless = Boolean.valueOf(System.getProperty("headless", "true"));
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
        Page page = browser.newPage();

        page.route("**/*", route -> { // Block unnecessary resources
            String reqUrl = route.request().url();
            if (reqUrl.endsWith(".png") || reqUrl.endsWith(".jpg") || reqUrl.endsWith(".css")
                    || reqUrl.contains("ads")) {
                route.abort();
            } else {
                route.resume();
            }
        });

        while (true) {
            String[] urls = API.getNextURL();
            for (String url : urls) {
                long startTime = System.currentTimeMillis();
                crawlPage(url, page);
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                System.out.println("Crawled " + url + " in " + duration / 1000.0 + " s");
            }
        }

    }
}
