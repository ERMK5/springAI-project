package com.example.ai.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QwenEmbeddingConfig {

    @Value("${QWEN_API_KEY}")
    private String apiKey;

    private final String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private final String model = "qwen3.7-text-embedding";

    @Bean(name = "qwenEmbeddingModel")
    public OpenAiEmbeddingModel qwenEmbeddingModel() {
        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .model(model)
                .build();

        return OpenAiEmbeddingModel.builder()
                .options(options)
                .metadataMode(MetadataMode.EMBED)
                .build();
    }
}