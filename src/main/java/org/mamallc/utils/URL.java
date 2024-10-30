package org.mamallc.utils;

public class URL {
    private String url;
    private String anchor_text;
    private boolean respects_robots;

    public String getURL() {
        return url;
    }

    public URL(String url) {
        this.url = url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void setAnchorText(String anchor_text) {
        this.anchor_text = anchor_text;
    }

    public void setRespect(boolean respects_robots) {
        this.respects_robots = respects_robots;
    }
}
