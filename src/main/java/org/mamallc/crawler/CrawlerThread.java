package org.mamallc.crawler;

public class CrawlerThread extends Thread {

    @Override
    public void run() {
        Crawler cr = new Crawler();
        cr.fetchPage();
    }
}
