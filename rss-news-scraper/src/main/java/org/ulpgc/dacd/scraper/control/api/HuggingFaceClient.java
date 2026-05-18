package org.ulpgc.dacd.scraper.control.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HuggingFaceClient {
    private static final String API_BASE_URL = "https://router.huggingface.co/hf-inference/models/";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(20);
    private static final int MAX_RETRIES = 3;
    private static final int HTTP_OK = 200;
    private static final int HTTP_UNAVAILABLE = 503;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final long INITIAL_RETRY_DELAY_MS = 1500;
    private static final long SERVICE_UNAVAILABLE_DELAY_MS = 15000;
    private static final long RATE_LIMIT_DELAY_MS = 20000;

    private final String apiToken;
    private final HttpClient httpClient;

    public HuggingFaceClient(String apiToken) {
        validateToken(apiToken);
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    public String requestModelResponse(String rawText, String modelId) {
        String url = API_BASE_URL + modelId;
        String jsonPayload = buildJsonPayload(rawText);
        HttpRequest request = buildRequest(url, jsonPayload);

        return executeRequestWithRetries(request);
    }

    private String executeRequestWithRetries(HttpRequest request) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                Thread.sleep(INITIAL_RETRY_DELAY_MS);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                int status = response.statusCode();
                if (status == HTTP_OK) return response.body();

                handleErrorStatus(status);
            } catch (Exception e) {
                System.err.println("Request attempt failed: " + e.getMessage());
                break;
            }
        }
        return "";
    }

    private void handleErrorStatus(int status) throws InterruptedException {
        if (status == HTTP_UNAVAILABLE) {
            Thread.sleep(SERVICE_UNAVAILABLE_DELAY_MS);
        } else if (status == HTTP_TOO_MANY_REQUESTS) {
            Thread.sleep(RATE_LIMIT_DELAY_MS);
        } else {
            throw new RuntimeException("Unexpected HTTP status: " + status);
        }
    }

    private HttpRequest buildRequest(String url, String jsonPayload) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
    }

    private String buildJsonPayload(String text) {
        String sanitizedText = text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replaceAll("[\\n\\r\\t]+", " ");
        return "{\"inputs\": \"" + sanitizedText + "\"}";
    }

    private void validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("API Token cannot be null or empty");
        }
    }
}