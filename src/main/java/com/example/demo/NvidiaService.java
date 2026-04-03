package com.example.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

@Service
public class NvidiaService {
    private final WebClient webClient;

    public NvidiaService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://integrate.api.nvidia.com/v1/chat/completions")
                .defaultHeader("Authorization",
                        "Bearer " + "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")
                .defaultHeader("Accept", "text/event-stream")
                .build();
    }

    public Flux<Object> streamResponse(RequestDto request) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", request.getModel());

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", request.getPrompt());
        messages.add(msg);

        payload.put("messages", messages);
        payload.put("max_tokens", 2048);
        payload.put("temperature", 0.15);
        payload.put("top_p", 1.0);
        payload.put("presence_penalty", 0);
        payload.put("stream", true);
        payload.put("frequency_penalty", 0.0);
        payload.put("stream", true);
        Map<String, Object> chatTemplate = new HashMap<>();
        chatTemplate.put("enable_thinking", request.isThinking());
        ObjectMapper objectMapper = new ObjectMapper();
        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToFlux(String.class)
                .map(chunk -> {

                    System.out.println("Received chunk: " + chunk);
                    if (chunk.contains("[DONE]")) {
                        return (Object) new StreamResponse("[DONE]");
                    }
                    try {
                        String json = chunk.replace("data: ", "").trim();
                        if (json.isEmpty())
                            return new StreamResponse("");
                        JsonNode rootNode = objectMapper.readTree(json);
                        // ✅ Handle error chunk
                        // if (rootNode.has("error")) {
                        // String errMsg = rootNode.path("error").path("message").asText("Unknown
                        // error");
                        // System.out.println("API Error: " + errMsg);
                        // return new StreamResponse("[ERROR] " + errMsg);
                        // }
                        JsonNode choices = rootNode.path("choices");

                        // ✅ Check if choices exists and is array
                        if (!choices.isArray() || choices.size() == 0) {
                            return new StreamResponse("");
                        }
                        JsonNode delta = choices.get(0).path("delta");
                        // ✅ content may not exist
                        if (!delta.has("content")) {
                            return new StreamResponse("");
                        }
                        String content = delta.path("content").asText("");
                        if (delta.has("reasoning")) {
                            String reasoning = delta.path("reasoning").asText("");
                            StreamResponse response = new StreamResponse(content);
                            response.setReasoning(reasoning);
                            return response;
                        }
                        return new StreamResponse(content);

                    } catch (Exception e) {
                        e.printStackTrace();
                        return new StreamResponse("");
                    }
                })
                .onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.class,
                        ex -> {
                            System.out.println("Status: " + ex.getStatusCode());
                            System.out.println("Body: " + ex.getResponseBodyAsString());
                            return Flux.empty();
                        });
    }
}

class StreamResponse {
    private String content;
    private String reasoning;

    public StreamResponse(String content) {
        this.content = content;
    }

    public StreamResponse() {

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public String getReasoning() {
        return reasoning;
    }

}