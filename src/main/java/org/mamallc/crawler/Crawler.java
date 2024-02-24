package org.mamallc.crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.Scanner;

public class Crawler {
    String getRootURL(String url) {

        int i = 0;
        while (url.charAt(i) != '.') i++;
        while (url.charAt(i) != '/') i++;
        String rootURL = url.substring(0, i);

        return rootURL;
    }
    boolean checkRobotsTxt(String url) {
        boolean canVisit = true;
        System.out.println("Checking for "+url);
        String rootURL = getRootURL(url);
        try {
            File f = new File(rootURL + "/robots.txt");
            Scanner sc = new Scanner(f);
            while(sc.hasNext()) {
                String line = sc.nextLine();
                System.out.println(line);
            }
        } catch (FileNotFoundException fnf) {
            System.out.println(fnf+": robots.txt not found");
        }
        return canVisit;
    }
    public Page fetchPage(String url) {
        String content = null;
        Page pg = new Page();
        URLConnection conn = null;

        try {
            conn = new URL(url).openConnection();
            System.out.println("Content type: "+conn.getContentType());
            Scanner sc = new Scanner(conn.getInputStream());
            sc.useDelimiter("\\Z");
            content = sc.next();
            sc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Parsing the HTML for words
        try {
            StringBuilder word = new StringBuilder();
            int i = 0;
            while(i < Objects.requireNonNull(content).length()) {
                char ch = content.charAt(i);
                if (word.length() > 26) {
                    word = new StringBuilder();
                }
                // if (ch != '<' && ch != ' ' && ch != ',' && ch != '-' && ch != ';' && ch != '\'' && ch != '(' && ch != ')' && ch != '/' && ch != '.' && ch != '"' && ch != '\n' && ch != ':') {
                    // word.append(ch);
                // } else {
                    // Skip the tags
                    if (content.charAt(i) == '<' || content.charAt(i) == ' ') {
                        if (!word.isEmpty()) {
                            String textWord = word.toString().trim().toLowerCase();
                            pg.textSet.putIfAbsent(textWord, 0);
                            pg.textSet.put(textWord, pg.textSet.get(textWord) + 1);
                            word = new StringBuilder();
                        }
                        StringBuilder htmlTag = new StringBuilder();
                        if (content.charAt(i) == '<') {
                            while (content.charAt(++i) != '>') {
                                htmlTag.append(content.charAt(i));
                            }

                            // Get href from <a> tag
                            System.out.println(htmlTag);
                            String htmlTagString = htmlTag.toString();
                            if (htmlTag.charAt(0) == 'a' && htmlTag.charAt(1) == ' ') {
                                int j = 0;
                                StringBuilder nestedURL = new StringBuilder();
                                try {
                                    while (htmlTagString.charAt(++j) != 'h') ;
                                    while (htmlTagString.charAt(++j) != '"' && htmlTagString.charAt(j) != '\'') ;
                                    while (htmlTagString.charAt(++j) != '"' && htmlTagString.charAt(j) != '\'') {
                                        nestedURL.append(htmlTagString.charAt(j));
                                    }
                                } catch (StringIndexOutOfBoundsException siobe) {
                                    System.out.println(siobe + ": Incorrect URL format, skipping...");
                                }
                                String rootURL = getRootURL(url);
                                if (nestedURL.toString().contains(rootURL)) {
                                    System.out.println("URL: " + nestedURL);
                                    checkRobotsTxt(nestedURL.toString());
                                } else {
                                    StringBuilder fullURL = new StringBuilder(rootURL);
                                    fullURL.append(nestedURL);
                                    System.out.println("full url "+fullURL);
                                    checkRobotsTxt(fullURL.toString());
                                }
                            } else if (htmlTagString.equals("script") || htmlTagString.equals("style")) {
                                // Shit code
                                while (content.charAt(i) != '/' && content.charAt(i + 1) != 's') i++;
                            }
                        }
                    }
                }
            //}
        } catch (Exception exp) {
            System.out.println(exp);
        }
        return pg;
    }
}
