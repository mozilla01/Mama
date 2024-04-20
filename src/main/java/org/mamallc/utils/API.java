package org.mamallc.utils;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.List;

public class API {
    private static String APIURLPage = "http://192.168.0.105:8000/api";

    public static int insertPage(List<String> urls, String url) {
        int id = 0;
        try {
            Page page = new Page();
            page.setUrl(url);
            page.setURLS(urls);
            Gson gson = new Gson();
            String jsonRequest = gson.toJson(page);
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(new URI(APIURLPage + "/add-page/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println(postResponse.body());

            id = Integer.parseInt(postResponse.body());

        } catch (Exception e) {
            System.out.println("Error in thread " + Thread.currentThread().getName() + ": " + e);
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
                    .uri(new URI(APIURLPage + "/create-text-indices/"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
        }
    }

    public static String getNextURL() {
        String urlString = null;
        try {
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(new URI(APIURLPage + "/dequeue/"))
                    .DELETE()
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

            String str = postResponse.body();
            int end = str.length() - 1;

            // The string is returned with double quotes at beginning and end. So,
            urlString = str.substring(1, end);
        } catch (Exception e) {
            System.out.println("Getting next URL error: " + e);
        }
        return urlString;
    }

}
