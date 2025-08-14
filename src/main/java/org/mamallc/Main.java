package org.mamallc;

import org.mamallc.crawler.Crawler;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        Map<String, String> config = new HashMap<>();
        for (String arg : args) {
            String[] parts = arg.split("=");
            if (parts.length == 2) {
                config.put(parts[0], parts[1]);
            } else {
                System.out.println("Invalid argument: " + arg);
                System.out.println("Usage: -Dexec.args=\"--arg1=value1 --arg2=value2\"");
                System.exit(1);
            }
        }

        try {
            Crawler crawler = new Crawler();
            crawler.crawl(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
