package org.mamallc.crawler;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;

public class CrawlerThread extends Thread {

    @Override
    public void run() {
        Crawler cr = new Crawler();
        cr.fetchPage();
    }
}
