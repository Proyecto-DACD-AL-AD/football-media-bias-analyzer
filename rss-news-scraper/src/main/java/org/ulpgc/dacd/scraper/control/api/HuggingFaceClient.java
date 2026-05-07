package org.ulpgc.dacd.scraper.control.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HuggingFaceClient {
    private final String apiToken;
    private final HttpClient httpClient;

    public HuggingFaceClient(String apiToken) {
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public String analyze(String text, String modelId) {
        if (apiToken == null || apiToken.isEmpty()) {
            throw new IllegalArgumentException();
        }

        String url = "https://router.huggingface.co/hf-inference/models/" + modelId;
        String safeText = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ").replace("\t", " ");
        String jsonBody = "{\"inputs\": \"" + safeText + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                Thread.sleep(1500);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return response.body();
                } else if (response.statusCode() == 503) {
                    Thread.sleep(15000);
                } else if (response.statusCode() == 429) {
                    Thread.sleep(20000);
                } else {
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }
        return null;
    }
}