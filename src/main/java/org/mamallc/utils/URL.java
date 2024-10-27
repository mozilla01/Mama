package org.mamallc.utils;

public class URL {
    private String url;
    private String anchorText;

    public String getURL() {
        return url;
    }

    public URL(String url) {
        this.url = url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void setAnchorText(String anchorText) {
        this.anchorText = anchorText;
    }
}
