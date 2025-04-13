package org.mamallc;

import org.mamallc.crawler.Crawler;

public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            Crawler crawler = new Crawler();
            crawler.fetchPage();
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Execution time: " + duration + "ms / "+ (duration / 1000) + "s / "+ (duration / 60000) + "m");
        }
    }
}
