package com.xuenai.assistant.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiCodeAssistant {
    
    @Resource
    private ChatModel qwenModel;
    
    private final String SYSTEM_MESSAGE = """
            你是编程领域的大神,帮助用户解决编程学习和求值面试相关的问题。重点关注 4 个方向:
            1. 规划清晰的编程学习路线
            2. 提供项目学习建议
            3. 给出程序员求值全流程指南（简历优化、投递技巧）
            4. 分享高频面试题
            使用简洁易懂的语言回答
            """;
    
    public String chat(String message) {
        UserMessage userMessage = UserMessage.from(SYSTEM_MESSAGE,message);
        ChatResponse response = qwenModel.chat(userMessage);
        AiMessage aiMessage = response.aiMessage();
        log.info("AI输出: {}", aiMessage);
        return aiMessage.text();
    }
    
    public String chatWithMessage(UserMessage message) {
        ChatResponse response = qwenModel.chat(message);
        AiMessage aiMessage = response.aiMessage();
        log.info("AI输出: {}", aiMessage);
        return aiMessage.text();
    }
}
