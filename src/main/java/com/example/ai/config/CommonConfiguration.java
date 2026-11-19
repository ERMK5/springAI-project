package com.example.ai.config;

import com.example.ai.constants.SystemConstants;
import com.example.ai.tools.CourseTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.vectorstore.VectorStore;

@Configuration
public class CommonConfiguration {

    /**
     * 添加会话记忆Advisor
     *
     * @return
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory
                .builder()
                .maxMessages(10)
                .build();
    }

    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient
                .builder(model) // 创建ChatClient工厂实例
                .defaultSystem("你将扮演游戏《明日方舟》中的角色凯尔希，请以凯尔希的身份和语气回答问题。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .build()
                ) // 添加Advisor,记录日志
                .build(); // 构建ChatClient实例
    }

    @Bean
    public ChatClient gameChatClient(OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient
                .builder(model) // 创建ChatClient工厂实例
                .defaultSystem(SystemConstants.GAME_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .build()
                ) // 添加Advisor,记录日志
                .build(); // 构建ChatClient实例
    }

    @Bean
    public ChatClient serviceChatClient(OpenAiChatModel model, ChatMemory chatMemory, CourseTools courseTools) {
        return ChatClient
                .builder(model) // 创建ChatClient工厂实例
                .defaultSystem(SystemConstants.SERVICE_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .build()
                ) // 添加Advisor,记录日志
                .defaultTools(courseTools)// 配置工具，SpringAI依然是基于AOP的能力，在请求大模型时会把我们定义的工具信息拼接到提示词中
                .build(); // 构建ChatClient实例
    }

    @Bean
    public ChatClient pdfChatClient(
            OpenAiChatModel model,
            ChatMemory chatMemory,
            VectorStore vectorStore) {
        return ChatClient.builder(model)
                .defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor
                                .builder(vectorStore)
                                .searchRequest(
                                        SearchRequest.builder() // 向量检索的请求参数
                                                .similarityThreshold(0.5d) // 相似度阈值
                                                .topK(2) // 返回的文档片段数量
                                                .build()
                                ).build()
                )
                .build();
    }

}
