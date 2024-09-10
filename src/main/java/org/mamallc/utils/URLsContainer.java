package org.mamallc.utils;

import java.util.Set;
import java.util.HashSet;

public class URLsContainer {
    Set<org.mamallc.utils.URL> urls;

    URLsContainer (Set<org.mamallc.utils.URL> urls) {
        this.urls = new HashSet<>(urls);
    }
}
