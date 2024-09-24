package org.mamallc.utils;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class API {

    private static String APIURL = "http://127.0.0.1:8000";

    public static void insertCrawlEntry(
        Set<org.mamallc.utils.URL> urls,
        String url,
        Set<String> queueOfStrings,
        List<String> textList
    ) {
        try {
            Page page = new Page();
            page.setURL(url);
            page.setOutgoingURLS(queueOfStrings);
            page.setLastDate(new Date().toInstant().toString());
            page.setText(textList);
            Gson gson = new Gson();
            String jsonRequest = gson.toJson(page);
            System.out.println(jsonRequest);
            /*
             * Add Page
             * Add page entry to Pages database
             */
            URL endpoint = new URI(APIURL + "/pages/create-page").toURL();

            HttpURLConnection con =
                (HttpURLConnection) endpoint.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                        con.getInputStream(),
                        StandardCharsets.UTF_8
                    )
                )
            ) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(con.getResponseCode() + " " + response);
            }
            /*
             * Enqueue URLs
             * Add crawlable URLs to the Queue
             */
            URLsContainer urlsContainer = new URLsContainer(urls);
            jsonRequest = gson.toJson(urlsContainer);
            System.out.println(jsonRequest);
            endpoint = new URI(APIURL + "/enqueue").toURL();

            con = (HttpURLConnection) endpoint.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                        con.getInputStream(),
                        StandardCharsets.UTF_8
                    )
                )
            ) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(con.getResponseCode() + " " + response);
            }
        } catch (Exception e) {
            System.out.println(
                "Error in thread " + Thread.currentThread().getName() + ": " + e
            );
        }
    }

    public static String getNextURL() {
        String urlString = null;
        Gson gson = new Gson();
        try {
            HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(APIURL + "/dequeue"))
                .DELETE()
                .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<String> postResponse = httpClient.send(
                postRequest,
                HttpResponse.BodyHandlers.ofString()
            );

            urlString = gson.fromJson(postResponse.body(), String.class);
        } catch (Exception e) {
            System.out.println(e);
        }
        return urlString;
    }
}
