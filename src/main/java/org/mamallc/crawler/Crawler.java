package org.mamallc.crawler;

import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
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
        System.out.println("Checking for "+url);
        URLConnection conn = null;
        try {
            conn = new URL(rootURL + "/robots.txt").openConnection();
            Scanner sc = new Scanner(conn.getInputStream());
            sc.useDelimiter("\n");
            System.out.println("Robots file");
            while(sc.hasNext()) {
                System.out.println("Looping through file");
                String line = sc.nextLine();
                String arr[] = line.split(":");
                if (arr.length > 1) {
                    if (arr[0].trim().equals("User-agent") && arr[1].trim().equals("*")) {
                        String rule[] = sc.nextLine().split(":");
                        while (rule.length < 2) rule = sc.nextLine().split(":");
                        String directive = rule[0].trim();
                        String route = rule[1].trim();
                        while (directive.equals("Disallow") || directive.equals("Allow")) {
                            System.out.println("Looping through rule");
                            Pattern regex = Pattern.compile(route);
                            Matcher matcher = regex.matcher(url);
                            if (directive.equals("Disallow") && matcher.find()) {
                                canVisit = false;
                            }
                            if (directive.equals("Allow") && matcher.find()) {
                                canVisit = true;
                            }
                            rule = sc.nextLine().split(":");
                            directive = rule[0].trim();
                            route = rule[1].trim();
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
    public Page fetchPage(String url) {
        String content = null;
        Page pg = new Page();
        URLConnection conn = null;

        try {
            conn = new URL(url).openConnection();
            Scanner sc = new Scanner(conn.getInputStream());
            sc.useDelimiter("\\Z");
            content = sc.next();
            sc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Parsing the HTML for words
            StringBuilder word = new StringBuilder();
            int i = 0;
            System.out.println(content);
            while(i < Objects.requireNonNull(content).length()) {

                // We want to skip the html tags and their attributes from the text index
                if (content.charAt(i) == '<') {
                    // In case the tag is an <a> tag, we want to extract the href
                    if (content.charAt(i+1) == 'a' && content.charAt(i+2) == ' ') {
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
                            System.out.println("Formed after processing URL :"+completeURL);

                            System.out.println("-------Robot file checking---------");
                            if (checkRobotsTxt(completeURL.toString(), rootURL)) {
                                System.out.println("Allowed");
                            } else {
                                System.out.println("Not allowed");
                            }
                        } catch (StringIndexOutOfBoundsException siob) {
                            System.out.println("Incorrect URL format: "+siob);
                        }
                    }
                    while (content.charAt(++i) != '>');
                    i++;
                    continue;
                }

                // Start building the word
                StringBuilder text = new StringBuilder();
                while(content.charAt(i) != ' ' && content.charAt(i) != '>' && content.charAt(i) != ')' && content.charAt(i) != '_' && content.charAt(i) != '#' && content.charAt(i) != '(' && content.charAt(i) != '\\' && content.charAt(i) != '/' && content.charAt(i) != '$' && content.charAt(i) != '"' && content.charAt(i) != '\'' && content.charAt(i) != '-' && content.charAt(i) != '<' && content.charAt(i) != ',' && content.charAt(i) != '\r' && content.charAt(i) != '\n') {
                    text.append(content.charAt(i));
                    i++;
                }
                if (text.length() > 25) text = new StringBuilder();
                if (!text.isEmpty()) {
                    String processedText = text.toString().trim().toLowerCase();
                    pg.textSet.putIfAbsent(processedText, 0);
                    pg.textSet.put(processedText, pg.textSet.get(processedText) + 1);
                }
                i++;
            }
        return pg;
    }
}
