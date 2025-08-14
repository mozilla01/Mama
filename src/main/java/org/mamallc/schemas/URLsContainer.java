package org.mamallc.schemas;

import java.util.Set;
import java.util.HashSet;

public class URLsContainer {
    Set<org.mamallc.schemas.URL> urls;

    public URLsContainer(Set<org.mamallc.schemas.URL> urls) {
        this.urls = new HashSet<>(urls);
    }
}
