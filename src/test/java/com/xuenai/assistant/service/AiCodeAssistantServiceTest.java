package com.xuenai.assistant.service;

import dev.langchain4j.service.Result;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeAssistantServiceTest {

    @Resource
    private AiCodeAssistantService aiCodeAssistantService;
    
    @Test
    void chat() {
        String result = aiCodeAssistantService.chat("你好，我想学习Java");
        System.out.println(result);
    }

    @Test
    void chatFormResultMessage() {
        String message = "你好，我是小菜，我现在想要学习 Java";
        AiCodeAssistantService.ChatResultMessage result = aiCodeAssistantService.chatFormResultMessage(message);
        System.out.println(String.format("你好, %s ,这是我给你的建议",result.name()));
        System.out.println("建议是: ");
        result.content().forEach(System.out::println);
    }

    @Test
    void chatFormResult() {
        String message = "什么是 RabbitMQ？怎么学习 RabbitMQ？";
        Result<String> result = aiCodeAssistantService.chatFormResult(message);
        System.out.println(result.content());
        System.out.println(result.sources());
    }

    @Test
    void chatFormTool() {
        String result = aiCodeAssistantService.chat("你好，常见的 Kafka 的面试题有哪些？");
        System.out.println(result);
    }

    @Test
    void chatFormMCP() {
        String result = aiCodeAssistantService.chat("你知道谁是程序员鱼皮吗？");
        System.out.println(result);
    }

    @Test
    void chatFormGuardrail() {
        String result = aiCodeAssistantService.chat("kill the game");
        System.out.println(result);
    }

    @Test
    void chatFormLog() {
        String result = aiCodeAssistantService.chat("你是谁？");
        System.out.println(result);
    }
}