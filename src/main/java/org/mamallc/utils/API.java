package org.mamallc.utils;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Set;

public class API {
    private static String APIURL = "http://127.0.0.1:8000/api";
    public static int insertPage(String url) {
        int id = 0;
        try {
            Page page = new Page();
            page.setUrl(url);
            Gson gson = new Gson();
            String jsonRequest = gson.toJson(page);
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(new URI(APIURL+"/add-page/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

            page = gson.fromJson(postResponse.body(), Page.class);
            id = page.getId();

        } catch (Exception e) {
            System.out.println(e);
        }
        return id;
    }

    public static void insertTextIndices(Map<String, Integer> mp, int page) {
        TextIndex[] textIndex = new TextIndex[mp.size()];
        int count = 0;
        for (Map.Entry<String, Integer> entry : mp.entrySet()) {
            textIndex[count] = new TextIndex();
            textIndex[count].setPage(page);
            textIndex[count].setWord(entry.getKey());
            textIndex[count].setFrequency(entry.getValue());
            count++;
        }
        try {
            Gson gson = new Gson();
            String jsonRequest = gson.toJson(textIndex);
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(new URI(APIURL+"/create-text-indices/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public static String getNextURL() {
        URL url;
        String urlString = null;
        try {
            Gson gson = new Gson();
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(new URI(APIURL+"/dequeue/"))
                    .DELETE()
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

            String str = postResponse.body();
            int end = str.length() - 1;
            urlString = str.substring(1, end);
        } catch (Exception e) {
            System.out.println(e);
        }
        return urlString;
    }
    public static void enqueueURLs(Set<String> set) {
        URL url[] = new URL[set.size()];
        Gson gson = new Gson();

        int count= 0;
        for (String urlString : set.stream().toList()) {
            url[count] = new URL();
            url[count].setUrl(urlString);
            count++;
        }
        String jsonRequest = gson.toJson(url);
        try {
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(new URI(APIURL + "/enqueue/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
