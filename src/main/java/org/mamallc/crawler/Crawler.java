package org.mamallc.crawler;

import org.mamallc.utils.API;

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

    String getRootURL(String url) {

        int i = 0;
        while (url.charAt(i) != '.')
            i++;
        while (i < url.length() && url.charAt(i) != '/')
            i++;
        String rootURL = url.substring(0, i);

        return rootURL;
    }

    boolean checkForProtocol(String url) {
        StringBuilder protocol = new StringBuilder();
        url = url.trim();
        boolean valid = true;
        int i = 0;
        while (i < url.length() && i < 4) {
            protocol.append(url.charAt(i));
            i++;
        }
        if (!protocol.toString().equals("http"))
            valid = false;
        return valid;
    }

    boolean checkRobotsTxt(String url, String rootURL) {
        boolean canVisit = true;
        URLConnection conn = null;
        try {
            conn = new URI(rootURL + "/robots.txt").toURL().openConnection();
            Scanner sc = new Scanner(conn.getInputStream());
            sc.useDelimiter("\n");
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

    public void fetchPage() {
        while (true) {
            Set<String> queue = new HashSet<>();
            // String url = API.getNextURL();
            String url = "https://www.reddit.com/r/LocalLLaMA/";

            System.out.println("--------------------------------------");
            System.out.println("Now crawling " + url);
            System.out.println("--------------------------------------");

            String content = null;
            Page pg = new Page();
            URLConnection conn = null;

            try {
                conn = new URI(url).toURL().openConnection();
                String contentEncoding = conn.getHeaderField("Content-Encoding");

                Scanner sc = new Scanner(conn.getInputStream());
                sc.useDelimiter("\\Z");
                content = sc.next();
                if (contentEncoding != null && contentEncoding.equals("gzip")) {
                    // content = decompress(content);
                    content = "";
                }
                sc.close();
            } catch (Exception ex) {
                ex.printStackTrace();
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

            StringBuilder nestedURL = new StringBuilder();
            StringBuilder title = new StringBuilder();
            StringBuilder sentence = new StringBuilder();
            StringBuilder tag = new StringBuilder();

            if (content != null || !content.isEmpty() || !content.isBlank()) {
                for (int stringPointer = 0; stringPointer < content.length(); stringPointer++) {
                    //Reaching end of opening tag
                    // Either <tag> or <tag attr="kadfl" ...
                    if ((content.charAt(stringPointer) == ' ' || content.charAt(stringPointer) == '>') && withinAngledBraces) {
                        if (content.charAt(stringPointer) == '>') withinAngledBraces = false;
                        if (withinTag) {
                            String tagString = tag.toString();
                            withinTag = false;
                            // We need to know which tag we are about to enter or leave
                            if (tagString.equals("script")) withinScript = true;
                            if (tagString.equals("/script") && withinScript) withinScript = false;
                            if (tagString.equals("style")) withinStyle = true;
                            if (tagString.equals("/style") && withinStyle) withinStyle = false;
                            if (tagString.equals("title") && !withinTitle) withinTitle = true;
                            if (!withinBody && tagString.equals("body")) withinBody = true;
                            if (withinBody && tagString.equals("/body")) withinBody = false;
                            if (tagString.equals("a")) withinAnchor = true;
                            tag = new StringBuilder();
                        }
                        continue;
                    }
                    if (withinURL && content.charAt(stringPointer) == '"') {
                        withinURL = false;
                        queue.add(nestedURL.toString());
                        nestedURL = new StringBuilder();
                    }

                    if (content.charAt(stringPointer) == '<' && !withinAngledBraces) {
                        withinAngledBraces = true;
                        withinTag = true;
                        if (withinTitle) withinTitle = false;
                        String sentenceString = sentence.toString().trim();
                        if (!sentenceString.isEmpty() || !sentenceString.isBlank())
                            pg.textList.add(sentenceString);
                        sentence = new StringBuilder();
                        continue;
                    }

                    if (withinURL) {
                        nestedURL.append(content.charAt(stringPointer));
                    }

                    if (withinTitle && !withinBody) {
                        title.append(content.charAt(stringPointer));
                    }

                    if (!withinAngledBraces && withinBody && !withinScript && !withinStyle) {
                        sentence.append(content.charAt(stringPointer));
                    }
                    
                    //Checking to see if we found the href attr in the anchor
                    if (withinAnchor && !withinURL && content.charAt(stringPointer) == 'h'
                            && content.charAt(stringPointer + 1) == 'r' && content.charAt(stringPointer + 2) == 'e'
                            && content.charAt(stringPointer + 3) == 'f') {
                        while (content.charAt(stringPointer) != '"')
                            stringPointer++;
                        withinURL = true;
                    }

                    if (withinTag)
                        tag.append(content.charAt(stringPointer));
                }
            }
            System.out.println("Queue: " + queue);
            System.out.println("Title: " + title.toString().trim());
            System.out.println("Text list: " + pg.textList);
        }
    }
}
