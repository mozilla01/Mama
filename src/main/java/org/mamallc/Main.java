package org.mamallc;

import org.mamallc.crawler.CrawlerThread;


public class Main {
    public static void main(String[] args) {

        for (int i = 0; i < 1; i++) {
            try {
                // Create a file handler for thread 1
                System.out.println("Starting thread "+i);

                CrawlerThread crawlerThread = new CrawlerThread();
                crawlerThread.start();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
