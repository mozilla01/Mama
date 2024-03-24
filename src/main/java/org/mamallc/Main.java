package org.mamallc;

import org.mamallc.crawler.Crawler;
import org.mamallc.crawler.CrawlerThread;
import org.mamallc.crawler.Page;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.Handler;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) {

        Logger logger = Logger.getLogger("");
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers) {
            logger.removeHandler(handler);
        }

        for (int i = 0; i < 8; i++) {
            try {
                // Create a file handler for thread 1
                System.out.println("Starting thread " + i);
                FileHandler thread1Handler = new FileHandler("logs/thread" + i + ".log");
                thread1Handler.setFormatter(new SimpleFormatter());
                logger.addHandler(thread1Handler);

                CrawlerThread crawlerThread = new CrawlerThread(Logger.getLogger("Thread" + i));
                crawlerThread.start();
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
    }
}
