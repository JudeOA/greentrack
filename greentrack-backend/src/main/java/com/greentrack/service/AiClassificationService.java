package com.greentrack.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.greentrack.dto.response.ClassificationResponse;
import com.greentrack.entity.Category;
import com.greentrack.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sends the uploaded waste photo to Google Gemini (vision) and asks it to
 * classify the image into one of the app's existing categories. Never throws:
 * on any failure it returns an empty suggestion so the report flow is safe.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiClassificationService {

    private final CategoryRepository categoryRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.gemini.api-key:}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-2.5-flash}")
    private String model;

    private static final ClassificationResponse EMPTY =
            ClassificationResponse.builder().categoryName(null).confidence(0).build();

    public ClassificationResponse classify(MultipartFile image) {
        try {
            if (apiKey == null || apiKey.isBlank() || image == null || image.isEmpty()) {
                log.warn("AI classify skipped: apiKeyPresent={}, imageEmpty={}",
                        (apiKey != null && !apiKey.isBlank()), (image == null || image.isEmpty()));
                return EMPTY;
            }

            List<String> names = categoryRepository.findAll().stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());
            if (names.isEmpty()) return EMPTY;

            byte[] bytes = image.getBytes();
            log.info("AI classify: received image bytes={}, contentType={}", bytes.length, image.getContentType());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String mime = image.getContentType();
            if (mime == null || !mime.startsWith("image/")) mime = "image/jpeg";

            String prompt = "Carefully LOOK at the attached photo of a real-world scene. "
                    + "First describe in a few words what you actually see in the image. "
                    + "Then classify the waste/sanitation issue into EXACTLY ONE of these categories: "
                    + String.join(", ", names) + ". "
                    + "Base your answer ONLY on what is visibly in the photo, not on guessing. "
                    + "Respond with ONLY a JSON object: "
                    + "{\"seen\":\"<short description of what is in the photo>\","
                    + "\"category\":\"<one of the exact category names above>\","
                    + "\"confidence\":<integer 0-100>}.";

            ObjectNode root = mapper.createObjectNode();
            ArrayNode contents = root.putArray("contents");
            ObjectNode content = contents.addObject();
            ArrayNode parts = content.putArray("parts");
            // Image FIRST, then the instruction (recommended ordering for vision)
            ObjectNode inline = parts.addObject().putObject("inline_data");
            inline.put("mime_type", mime);
            inline.put("data", base64);
            parts.addObject().put("text", prompt);
            ObjectNode gen = root.putObject("generationConfig");
            gen.put("temperature", 0);
            gen.put("responseMimeType", "application/json");

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(root)))
                    .build();

            HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                log.warn("Gemini classify HTTP {}: {}", resp.statusCode(), resp.body());
                return EMPTY;
            }

            JsonNode body = mapper.readTree(resp.body());
            JsonNode textNode = body.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text");
            if (textNode.isMissingNode()) {
                log.warn("Gemini classify: no text in response: {}", resp.body());
                return EMPTY;
            }

            String rawText = textNode.asText();
            log.info("Gemini classify raw output: {}", rawText);

            JsonNode parsed = mapper.readTree(rawText);
            String category = parsed.path("category").asText(null);
            int confidence = parsed.path("confidence").asInt(0);
            if (confidence < 0) confidence = 0;
            if (confidence > 100) confidence = 100;

            String matched = names.stream()
                    .filter(n -> n.equalsIgnoreCase(category == null ? "" : category.trim()))
                    .findFirst().orElse(null);
            if (matched == null) {
                log.warn("Gemini returned unknown category '{}'", category);
                return EMPTY;
            }

            return ClassificationResponse.builder()
                    .categoryName(matched)
                    .confidence(confidence)
                    .build();

        } catch (Exception e) {
            log.warn("AI classification failed: {}", e.getMessage(), e);
            return EMPTY;
        }
    }
}
