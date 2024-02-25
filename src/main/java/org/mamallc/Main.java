package org.mamallc;

import org.mamallc.crawler.Crawler;
import org.mamallc.crawler.Page;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello and welcome!");

        Crawler cr = new Crawler();
        Page pg = cr.fetchPage("https://amazon.in");

        System.out.println("=================Map entries=================");
        for (Map.Entry entry : pg.textSet.entrySet()) {
            String key = entry.getKey().toString();
            Integer val = (Integer) entry.getValue();
            System.out.println(key+"->"+val);
        }
    }
}