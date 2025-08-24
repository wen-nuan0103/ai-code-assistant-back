package com.xuenai.assistant.service;


import com.xuenai.assistant.guardrails.SafeInputGuardrail;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import reactor.core.publisher.Flux;

import java.util.List;

@InputGuardrails({SafeInputGuardrail.class})
public interface AiCodeAssistantService {
    
    record ChatResultMessage(String name, List<String> content) {}
    
    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(String message);

    @SystemMessage(fromResource = "system-prompt.txt")
    ChatResultMessage chatFormResultMessage(String message);

    @SystemMessage(fromResource = "system-prompt.txt")
    Result<String> chatFormResult(String message);

    @SystemMessage(fromResource = "system-prompt.txt")
    Flux<String> chatMessage(@MemoryId int memoryId,@UserMessage String message);
}
