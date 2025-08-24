    package com.xuenai.assistant.factory;

    import com.xuenai.assistant.service.AiCodeAssistantService;
    import dev.langchain4j.mcp.McpToolProvider;
    import dev.langchain4j.memory.chat.MessageWindowChatMemory;
    import dev.langchain4j.model.chat.ChatModel;
    import dev.langchain4j.model.chat.StreamingChatModel;
    import dev.langchain4j.rag.content.retriever.ContentRetriever;
    import dev.langchain4j.service.AiServices;
    import jakarta.annotation.Resource;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    
    @Configuration
    public class AiCodeAssistantServiceFactory {
    
        @Resource
        private ChatModel myQwenChatModel;
        
        private StreamingChatModel qwenStreamingChatModel;
        
        @Resource
        private ContentRetriever contentRetriever;
        
        @Resource
        private McpToolProvider mcpToolProvider;
        
        @Bean
        public AiCodeAssistantService qwenService() {
            // 会话记忆
            MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
            AiCodeAssistantService aiCodeAssistantService = AiServices.builder(AiCodeAssistantService.class)
                    .chatModel(myQwenChatModel)
                    .streamingChatModel(qwenStreamingChatModel)
                    .chatMemory(chatMemory)
                    .chatMemoryProvider(id -> MessageWindowChatMemory.withMaxMessages(10))
                    .contentRetriever(contentRetriever)
//                    .tools(new InterviewQuestionTool())
                    .toolProvider(mcpToolProvider)
                    .build();
            return aiCodeAssistantService;
        }
        
    }
