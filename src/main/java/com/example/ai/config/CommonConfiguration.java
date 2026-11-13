package com.example.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfiguration {

    /**
     * 添加会话记忆Advisor
     * @return
     */
    @Bean
    public ChatMemory chatMemory(){
        return MessageWindowChatMemory
                .builder()
                .maxMessages(10)
                .build();
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

}
