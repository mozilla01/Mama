package org.mamallc;

import org.mamallc.crawler.Crawler;

public class Main {
    public static void main(String[] args) {
        try {
            Crawler crawler = new Crawler();
            crawler.fetchPage();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
