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

                // We want to skip the html tags and their attributes from the text index
                if (content.charAt(i) == '<') {
                    // In case the tag is an <a> tag, we want to extract the href
                    if (content.charAt(i+1) == 'a' && content.charAt(i+2) == ' ') {
                        StringBuilder nestedURL = new StringBuilder();
                        while(content.charAt(++i) != 'h');
                        while(content.charAt(++i) != '"' && content.charAt(i) != '\'');
                        while(content.charAt(++i) != '"' && content.charAt(i) != '\'') {
                            nestedURL.append(content.charAt(i));
                        }
                        System.out.println("URL: "+nestedURL);
                    }
                    while (content.charAt(++i) != '>');
                    i++;
                    continue;
                }
                StringBuilder text = new StringBuilder();
                while(content.charAt(i) != ' ' && content.charAt(i) != '"' && content.charAt(i) != '\'' && content.charAt(i) != '-' && content.charAt(i) != '<' && content.charAt(i) != ',' && content.charAt(i) != '\r' && content.charAt(i) != '\n') {
                    text.append(content.charAt(i));
                    i++;
                }
                if (!text.isEmpty()) {
                    String processedText = text.toString().trim().toLowerCase();
                    pg.textSet.putIfAbsent(processedText, 0);
                    pg.textSet.put(processedText, pg.textSet.get(processedText) + 1);
                }
                i++;
            }
        } catch (Exception exp) {
            System.out.println(exp);
        }
        return pg;
    }
}
