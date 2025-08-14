package org.mamallc.schemas;

public class URL {
    private String url;
    private String anchor_text;

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

}
