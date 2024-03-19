package org.mamallc.crawler;

import org.mamallc.utils.API;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class Crawler {
    private Logger logger;
    public Crawler(Logger logger) {
        this.logger = logger;
    }

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
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }

    String getRootURL(String url) {

        int i = 0;
        while (url.charAt(i) != '.') i++;
        while (i < url.length() && url.charAt(i) != '/') i++;
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
        if (!protocol.toString().equals("http")) valid = false;
        return valid;
    }

    boolean checkRobotsTxt(String url, String rootURL) {
        boolean canVisit = true;
//        logger.info("robots.txt: Checking for " + url);
        URLConnection conn = null;
        try {
            conn = new URL(rootURL + "/robots.txt").openConnection();
            Scanner sc = new Scanner(conn.getInputStream());
            sc.useDelimiter("\n");
            while (sc.hasNext()) {
                String line = sc.nextLine();
                String arr[] = line.split(":");
                if (arr.length > 1) {
                    if (arr[0].trim().equals("User-agent") && arr[1].trim().equals("*")) {
                        String rule[] = sc.nextLine().split(":");
                        while (rule.length < 2) rule = sc.nextLine().split(":");
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
            logger.info(e.toString());
        }
        return canVisit;
    }
    public boolean isPrintableChar( char c ) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }
    public void fetchPage() {
        while (true) {
            Set<String> queue = new HashSet<>();
            String url = API.getNextURL();

            logger.info("--------------------------------------");
            logger.info("Now crawling "+url);
            logger.info("--------------------------------------");

            String content = null;
            Page pg = new Page();
            URLConnection conn = null;

            try {
                conn = new URL(url).openConnection();
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
            int i = 0;
            if (content != null && !content.isEmpty())
                while (i < Objects.requireNonNull(content).length()) {
                    // We want to skip the html tags and their attributes from the text index
                    if (content.charAt(i) == '<') {
                        // In case the tag is an <a> tag, we want to extract the href
                        if (content.charAt(i + 1) == 'a' && content.charAt(i + 2) == ' ') {
                            try {
                                StringBuilder nestedURL = new StringBuilder();
                                while (content.charAt(++i) != 'h') ;
                                while (content.charAt(++i) != '"' && content.charAt(i) != '\'') ;
                                while (content.charAt(++i) != '"' && content.charAt(i) != '\'') {
                                    nestedURL.append(content.charAt(i));
                                }

                                String rootURL = getRootURL(url);
                                StringBuilder completeURL = new StringBuilder(rootURL);
                                if (checkForProtocol(nestedURL.toString())) {
                                    completeURL = new StringBuilder(nestedURL);
                                } else {
                                    if (nestedURL.charAt(0) != '/') completeURL.append('/');
                                    completeURL.append(nestedURL);
                                }

                                logger.info("-------Robot file checking---------");
                                if (!queue.contains(completeURL.toString())) {
                                    if (checkRobotsTxt(completeURL.toString(), rootURL)) {
//                                        logger.info("Allowed: " + completeURL);
                                        queue.add(completeURL.toString());
                                    } else {
//                                        logger.info("Disallowed: " + completeURL);
                                    }
                                }
                            } catch (Exception e) {
                                logger.info("Incorrect URL format: "+e);
                            }
                        }
                        else if (content.charAt(i + 1) == 's' && (content.charAt(i + 2) == 'c' || content.charAt(i+2) == 't') && (content.charAt(i + 3) == 'r' || content.charAt(i+3) == 'y')) {
                            while(content.charAt(i+1) != '<' || content.charAt(i+2) != '/' || content.charAt(i+3) != 's') {
                                i++;
                            }
                        }
                        while (content.charAt(++i) != '>') ;
                        i++;
                        continue;
                    }

                    // Start building the word
                    StringBuilder text = new StringBuilder();
                    try {
                        while (content.charAt(i) != ';' && content.charAt(i) != '\t' && content.charAt(i) != '\b' && content.charAt(i) != '\0' && content.charAt(i) != ' ' && content.charAt(i) != '?' && content.charAt(i) != '&' && content.charAt(i) != '>' && content.charAt(i) != ')' && content.charAt(i) != '_' && content.charAt(i) != '#' && content.charAt(i) != '(' && content.charAt(i) != '\\' && content.charAt(i) != '/' && content.charAt(i) != '$' && content.charAt(i) != '"' && content.charAt(i) != '\'' && content.charAt(i) != '-' && content.charAt(i) != '<' && content.charAt(i) != ',' && content.charAt(i) != '\r' && content.charAt(i) != '\n' && content.charAt(i) != '.') {
                            text.append(content.charAt(i));
                            i++;
                        }
                    } catch (StringIndexOutOfBoundsException siob) {
                        logger.info(siob.toString());
                    }
                    if (text.length() > 25) text = new StringBuilder();
                    if ((text.toString()) != null && !(text.toString().trim().isEmpty()) && !(text.toString().isBlank())) {
                        String processedText = text.toString().trim().toLowerCase();
                        pg.textSet.putIfAbsent(processedText, 0);
                        pg.textSet.put(processedText, pg.textSet.get(processedText) + 1);
                    }
                    i++;
                }

            int id = API.insertPage(url);
            API.insertTextIndices(pg.textSet, id);
            API.enqueueURLs(queue);
        }
    }
}
