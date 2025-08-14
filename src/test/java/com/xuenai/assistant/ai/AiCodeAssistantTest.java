package com.xuenai.assistant.ai;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeAssistantTest {

    @Resource
    private AiCodeAssistant aiCodeAssistant;
    
    @Test
    void chat() {
//        System.out.println("测试");
        aiCodeAssistant.chat("你好,我想学习Java");
        
    }

    @Test
    void chatWithMessage() {

        UserMessage userMessage = UserMessage.from(
                TextContent.from("请描述这个图片"),
                ImageContent.from("src/main/resources/static/star.jpg")
        );
        aiCodeAssistant.chatWithMessage(userMessage);

    }
}