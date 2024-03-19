package org.mamallc.crawler;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;

public class CrawlerThread extends Thread {
    private Logger logger;
    public CrawlerThread(Logger logger) {
        this.logger = logger;
    }
    @Override
    public void run() {
        logger.log(Level.INFO, "Starting thread...");
        Crawler cr = new Crawler(logger);
        cr.fetchPage();
    }
}
