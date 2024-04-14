package org.mamallc.utils;

import java.util.ArrayList;
import java.util.List;

public class Page {
    private String page_url;
    private List<String> urls;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return page_url;
    }

    public List<String> getURLs() {
        return urls;
    }

    public void setUrl(String url) {
        this.page_url = url;
    }

    public void setURLS(List<String> urls) {
        this.urls = new ArrayList<>(urls);
    }
}
