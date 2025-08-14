package org.mamallc.utils;

public class URLString {

    public static String removeUTFStrings(String str) {
        return str.replace("&#x2F;", "/").replace("&#x3D;", "=").replace("&#x26;", "&")
                .replace("&#x3F;", "?").replace("&#x25;", "%").replace("&#x3A;", ":")
                .replace("&#x2C;", ",").replace("&#x2D;", "-").replace("&#x2E;", ".").replace("&#x23;", "#")
                .replace("&#x3B;", ";").replace("&#x3C;", "<").replace("&#x3E;", ">").replace("&#x40;", "@")
                .replace("&#x5F;", "_").replace("&#x7E;", "~").replace("&#x5B;", "[").replace("&#x5D;", "]")
                .replace("&#x7B;", "{").replace("&#x7D;", "}").replace("&#x7C;", "|").replace("&#x60;", "`")
                .replace("&#x22;", "\"").replace("&#x27;", "'").replace("&#x3E;", ">").replace("&#x3C;", "<")
                .replace("&#x5C;", "\\").replace("&#x24;", "$").replace("&#x40;", "@").replace("&#x2B;", "+")
                .replace("&#x3D;", "=").replace("&#x2A;", "*").replace("&#x25;", "%").replace("&#x5E;", "^")
                .replace("&#x21;", "!").replace("&#x3F;", "?").replace("&#x40;", "@").replace("&#x2C;", ",")
                .replace("&#x2E;", ".").replace("&#x2F;", "/").replace("&#x3A;", ":").replace("&#x3B;", ";")
                .replace("&#x3D;", "=").replace("&#x3F;", "?").replace("&#x40;", "@").replace("&#x5B;", "[")
                .replace("&#x5C;", "\\").replace("&#x5D;", "]").replace("&#x5E;", "^").replace("&#x5F;", "_")
                .replace("&#x60;", "`").replace("&#x7B;", "{").replace("&#x7C;", "|").replace("&#x7D;", "}")
                .replace("&#x7E;", "~");
    }

    public static String getRootURL(String url) {
        String rootURL = "https://example.com";
        try {
            int i = 0;
            while (url.charAt(i) != '.')
                i++;
            while (i < url.length() && url.charAt(i) != '/')
                i++;
            rootURL = url.substring(0, i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootURL;
    }

    public static int checkForProtocol(String url) {
        StringBuilder protocol = new StringBuilder();
        url = url.trim();
        int valid = 0;
        int i = 0;
        while (i < url.length() && i < 4) {
            protocol.append(url.charAt(i));
            i++;
        }
        if (protocol.length() > 1 && protocol.charAt(0) == '/' && protocol.charAt(1) == '/')
            valid = 1; // Unusual URL, do not append
        else if ((protocol.length() > 1 && protocol.charAt(0) == '/' && protocol.charAt(1) != '/')
                || (protocol.length() == 1 && protocol.charAt(0) == '/'))
            valid = 2; // Simple nested route, append root
        else if (protocol.toString().equals("http"))
            valid = 3; // Regular URL, do not append
        else if (protocol.length() > 0 && (protocol.charAt(0) == '#' || protocol.charAt(0) == '?'))
            valid = 5; // Anchor or query string, do not append
        else
            valid = 4; // hide?p=1 something
        return valid;
    }

    public static String processURL(String nestedURLString, String url, String rootURL) {
        String finalString = "";
        int valid = checkForProtocol(nestedURLString);
        if (valid == 0 || valid == 3) {
            finalString = nestedURLString;
        } else if (valid == 1) {
            finalString = "https:" + nestedURLString;
        } else if (valid == 2) {
            finalString = rootURL + nestedURLString;
        } else if (valid == 4) {
            finalString = rootURL + "/" + nestedURLString;
        } else if (valid == 5) {
            finalString = url;
        }
        return finalString;
    }

}
