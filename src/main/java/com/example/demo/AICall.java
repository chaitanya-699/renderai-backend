package com.example.demo;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AICall {

    private final NvidiaService nvidiaService;

    public AICall(NvidiaService nvidiaService) {
        this.nvidiaService = nvidiaService;
    }

    @PostMapping(value = "/chat", produces = "text/event-stream")
    public Flux<Object> startResponse(@RequestBody RequestDto request) {
        System.out.println("Received message: " + request.getPrompt());
        return nvidiaService.streamResponse(request);
    }
}
