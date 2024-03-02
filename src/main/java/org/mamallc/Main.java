package org.mamallc;

import org.mamallc.crawler.Crawler;
import org.mamallc.crawler.CrawlerThread;
import org.mamallc.crawler.Page;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello and welcome!");

        for (int i = 0; i < 5; i++) {
            CrawlerThread crawlerThread = new CrawlerThread();
            crawlerThread.start();
        }
    }
}