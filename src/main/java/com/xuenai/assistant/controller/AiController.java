package com.xuenai.assistant.controller;

import com.xuenai.assistant.service.AiCodeAssistantService;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {
    
    @Resource
    private AiCodeAssistantService aiCodeAssistantService;
    
    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(int memoryId,String message){
        return aiCodeAssistantService.chatMessage(memoryId,message)
                .map(chunk -> ServerSentEvent.builder(chunk).data(chunk).build());
    }
    
}
