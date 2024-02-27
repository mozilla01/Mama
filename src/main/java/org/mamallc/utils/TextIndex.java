package org.mamallc.utils;

public class TextIndex {
    private int page;
    private String word;
    private int frequency;


    public String getWord() {
        return word;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
