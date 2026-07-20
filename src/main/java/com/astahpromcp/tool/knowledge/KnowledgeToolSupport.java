package com.astahpromcp.tool.knowledge;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.knowledge.outputdto.DocumentDTO;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.opendataloader.pdf.api.Config;
import org.opendataloader.pdf.api.OpenDataLoaderPDF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class KnowledgeToolSupport {

    private static final int CHUNK_SIZE = 51200; // characters 50KB (50 * 1024)
    private static final int CHUNK_OVERLAP_LINES = 3; // lines of the previous chunk repeated at the start of the next chunk

    // Reusable, thread-safe HTML-to-Markdown converter
    private static final FlexmarkHtmlConverter HTML_TO_MARKDOWN_CONVERTER = FlexmarkHtmlConverter.builder(
            new MutableDataSet().set(FlexmarkHtmlConverter.SETEXT_HEADINGS, false)).build();

    // Creates an HttpClient for fetching knowledge documents, with a bounded connect timeout
    public static HttpClient newHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(McpServerConfig.KNOWLEDGE_FETCH_CONNECT_TIMEOUT_SECONDS))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    // Opens a stream to the given URL with bounded connect and read timeouts
    public static InputStream openUrlStream(String urlString) throws IOException {
        URLConnection connection = URI.create(urlString).toURL().openConnection();
        connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(McpServerConfig.KNOWLEDGE_FETCH_CONNECT_TIMEOUT_SECONDS));
        connection.setReadTimeout((int) TimeUnit.SECONDS.toMillis(McpServerConfig.KNOWLEDGE_FETCH_REQUEST_TIMEOUT_SECONDS));
        return connection.getInputStream();
    }

    // Converts a PDF file to Markdown using OpenDataLoader PDF and saves it as <outputBaseName>.md in the output directory.
    public static synchronized String convertPdfToMarkdown(Path pdfFile, Path outputDirectory, String outputBaseName) throws IOException {
        Config config = new Config();
        config.setOutputFolder(outputDirectory.toString());
        config.setGenerateMarkdown(true);
        config.setGenerateJSON(false);
        config.setImageOutput(Config.IMAGE_OUTPUT_OFF);
        OpenDataLoaderPDF.processFile(pdfFile.toString(), config);

        // OpenDataLoader PDF names the output <pdfBaseName>.md; rename it to <outputBaseName>.md
        String pdfFileName = pdfFile.getFileName().toString();
        String generatedName = pdfFileName.replaceAll("(?i)\\.pdf$", "") + ".md";
        Path generatedPath = outputDirectory.resolve(generatedName);
        Path outputPath = outputDirectory.resolve(outputBaseName + ".md");
        if (!generatedPath.equals(outputPath)) {
            Files.move(generatedPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("Markdown saved to file: {}", outputPath.toAbsolutePath());

        return Files.readString(outputPath, StandardCharsets.UTF_8);
    }

    // Converts PDF bytes to Markdown via a temporary <outputBaseName>.pdf file in the output directory
    public static String convertPdfToMarkdown(byte[] pdfBytes, Path outputDirectory, String outputBaseName) throws IOException {
        Files.createDirectories(outputDirectory);
        Path pdfPath = outputDirectory.resolve(outputBaseName + ".pdf");
        Files.write(pdfPath, pdfBytes);
        try {
            return convertPdfToMarkdown(pdfPath, outputDirectory, outputBaseName);
        } finally {
            Files.deleteIfExists(pdfPath);
        }
    }

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

    // Splits the text into chunks with overlap
    public static List<String> splitTextWithOverlap(String text, int chunkSize, int overlapLines) {
        List<String> chunks = splitText(text, chunkSize);
        if (chunks.size() <= 1 || overlapLines <= 0) {
            return chunks;
        }

        List<String> overlappedChunks = new ArrayList<>(chunks.size());
        overlappedChunks.add(chunks.get(0));
        for (int i = 1; i < chunks.size(); i++) {
            overlappedChunks.add(lastLines(chunks.get(i - 1), overlapLines) + chunks.get(i));
        }

        return overlappedChunks;
    }

    // Returns the last `lineCount` lines of the text
    private static String lastLines(String text, int lineCount) {
        int index = text.endsWith("\n") ? text.length() - 1 : text.length();
        for (int i = 0; i < lineCount; i++) {
            int newlineIndex = text.lastIndexOf('\n', index - 1);
            if (newlineIndex < 0) {
                return text;
            }
            index = newlineIndex;
        }
        return text.substring(index + 1);
    }

    // Splits text into overlapping chunks and replaces the contents of contentCache with them.
    public static DocumentDTO chunkAndCache(String text, List<String> contentCache) {
        List<String> chunks = splitTextWithOverlap(text, CHUNK_SIZE, CHUNK_OVERLAP_LINES);
        if (chunks.isEmpty()) {
            chunks.add(""); // Ensure there is at least one empty chunk
        }

        contentCache.clear();
        contentCache.addAll(chunks);

        return new DocumentDTO(chunks.size(), chunks.get(0));
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

    public static String convertHtmlToMarkdown(String html) {
        Document document = Jsoup.parse(html);
        document.select("script, style, nav, header, footer, aside").remove();
        // Documentation pages often present code examples in editable textareas; treat them as code blocks
        for (Element textarea : document.select("textarea")) {
            textarea.tagName("pre");
        }
        Element content = document.selectFirst("main");
        if (content == null) {
            content = document.selectFirst("article");
        }
        if (content == null) {
            content = document.body();
        }
        String contentHtml = content != null ? content.outerHtml() : html;

        try {
            return HTML_TO_MARKDOWN_CONVERTER.convert(contentHtml);
        } catch (Throwable e) {
            log.warn("Failed to convert HTML to Markdown, falling back to plain text extraction: {}", e.toString());
            return Jsoup.parse(contentHtml).text();
        }
    }

    public static CompletableFuture<String> fetchAndParse(HttpClient httpClient, String urlString) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlString))
                        .timeout(Duration.ofSeconds(McpServerConfig.KNOWLEDGE_FETCH_REQUEST_TIMEOUT_SECONDS))
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
                
                return convertHtmlToMarkdown(response.body());

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
