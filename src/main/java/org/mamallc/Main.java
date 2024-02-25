package org.mamallc;

import org.mamallc.crawler.Crawler;
import org.mamallc.crawler.Page;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello and welcome!");

        Crawler cr = new Crawler();
        cr.fetchPage();
    }
}