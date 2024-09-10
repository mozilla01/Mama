package org.mamallc.utils;

import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Page {
    private String url;
    private Set<String> outgoing;
    private List<String> text;
    private String last_crawled;
    
    public String getURL() {
        return url;
    }

    public Set<String> getOutgoingURLs() {
        return outgoing;
    }

    public List<String> getText() {
        return text;
    }

    public void setText(List<String> text) {
        this.text = new ArrayList<>(text);
    }
    public void setURL(String url) {
        this.url = url;
    }

    public void setOutgoingURLS(Set<String> urls) {
        this.outgoing = new HashSet<>(urls);
    }

    public String getLastDate() {
        return last_crawled;
    }

    public void setLastDate(String date) {
        this.last_crawled = date;
    }

}
