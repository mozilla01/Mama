package org.mamallc.utils;

import java.net.URI;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ethics {

    public static boolean checkRobotsTxt(String url, String rootURL, Scanner sc) {
        boolean canVisit = true;
        URLConnection conn = null;
        String currentRoot = URLString.getRootURL(url); // Root URL of the URL we are checking
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

}
