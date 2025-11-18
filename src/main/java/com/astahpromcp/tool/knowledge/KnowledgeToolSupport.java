package com.astahpromcp.tool.knowledge;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class KnowledgeToolSupport {

    public static List<String> splitText(String text, int chunkSize) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(text.substring(i, Math.min(text.length(), i + chunkSize)));
        }

        return chunks;
    }

    public static List<String> readUrlsFromResource(Class<?> clazz, String resourceName) throws IOException {
        List<String> urls = new ArrayList<>();
        try (InputStream is = clazz.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourceName);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        urls.add(line.trim());
                    }
                }
            }
        }

        return urls;
    }

    public static CompletableFuture<String> fetchAndParse(HttpClient httpClient, String urlString) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlString))
                        .header("User-Agent", "Java HttpClient Bot")
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                // Check HTTP status code
                int statusCode = response.statusCode();
                if (statusCode == 403) {
                    log.warn("HTTP 403 Forbidden error when fetching {}: Access denied", urlString);
                    return "[Error fetching content from " + urlString + ": HTTP 403 Forbidden]";
                } else if (statusCode == 429) {
                    log.warn("HTTP 429 Too Many Requests error when fetching {}: Rate limit exceeded", urlString);
                    return "[Error fetching content from " + urlString + ": HTTP 429 Too Many Requests]";
                } else if (statusCode >= 400) {
                    log.warn("HTTP error when fetching {}: Status code {}", urlString, statusCode);
                    return "[Error fetching content from " + urlString + ": HTTP " + statusCode + "]";
                }
                
                return Jsoup.parse(response.body()).text();

            } catch (ConnectException e) {
                log.warn("Connection error when fetching {}: {}", urlString, e.getMessage());
                return "[Error fetching content from " + urlString + ": Connection failed]";
            } catch (SocketTimeoutException e) {
                log.warn("Socket timeout error when fetching {}: {}", urlString, e.getMessage());
                return "[Error fetching content from " + urlString + ": Socket timeout]";
            } catch (HttpTimeoutException e) {
                log.warn("HTTP timeout error when fetching {}: {}", urlString, e.getMessage());
                return "[Error fetching content from " + urlString + ": HTTP timeout]";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Request interrupted when fetching {}: {}", urlString, e.getMessage());
                return "[Error fetching content from " + urlString + ": Request interrupted]";
            } catch (Exception e) {
                log.warn("Failed to fetch or parse {}: {}", urlString, e.getMessage());
                return "[Error fetching content from " + urlString + "]";
            }
        });
    }
}
