package com.example.ai;

import com.example.ai.util.VectorDistanceUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.ai.document.Document;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class AiApplicationTests {

    // 自动注入向量模型
    @Autowired
    @Qualifier("qwenEmbeddingModel")
    private OpenAiEmbeddingModel embeddingModel;

    // 自动注入向量库
    @Autowired
    private VectorStore vectorStore;

    /**
     * 向量模型单元测试
     */
    @Test
    public void testEmbedding() {
        String query = "global conflicts";
        String[] texts = new String[]{
                "哈马斯称加沙下阶段停火谈判仍在进行 以方尚未做出承诺", // 匹配1
                "土耳其、芬兰、瑞典与北约代表将继续就瑞典“入约”问题进行谈判", // 匹配2
                "日本航空基地水井中检测出有机氟化物超标", // 无关
                "国家游泳中心（水立方）：恢复游泳、嬉水乐园等水上项目运营", // 无关
                "我国首次在空间站开展舱外辐射生物学暴露实验", // 无关
        };

        float[] queryVector = embeddingModel.embed(query);
        List<float[]> textVectors = embeddingModel.embed(Arrays.asList(texts));

        // 1. 计算欧氏距离
        System.out.println("========== 欧氏距离（越小越相似） ==========");
        System.out.println("与自己比较：" + VectorDistanceUtils.euclideanDistance(queryVector, queryVector)); // 必然是 0
        for (int i = 0; i < textVectors.size(); i++) {
            System.out.println("与第 " + (i + 1) + " 句比较：" + VectorDistanceUtils.euclideanDistance(queryVector, textVectors.get(i)));
        }

        // 2. 计算余弦距离
        System.out.println("\n========== 余弦距离（越小越相似，但数值受是否归一化影响） ==========");
        System.out.println("与自己比较：" + VectorDistanceUtils.cosineDistance(queryVector, queryVector)); // 必然也是 0
        for (int i = 0; i < textVectors.size(); i++) {
            System.out.println("与第 " + (i + 1) + " 句比较：" + VectorDistanceUtils.cosineDistance(queryVector, textVectors.get(i)));
        }
    }

    /**
     * 向量库单元测试
     */
    @Test
    public void testVectorStore() {
        Resource resource = new FileSystemResource("笔记.pdf");
        // 1.创建PDF的读取器
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource, // 文件源
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1) // 每1页PDF作为一个Document
                        .build()
        );
        // 2.读取PDF文档，拆分为Document
        List<Document> documents = reader.read();
        // 3.写入向量库
        vectorStore.add(documents);
        // 4.搜索
        SearchRequest request = SearchRequest.builder()
                .query("论语中教学的目的是什么")// 搜索词
                .topK(2)// 结果数量：最多只返回相似度最高的 5 条文档
                .similarityThreshold(0.5)// 相似度阈值：过滤掉得分低于 0.5 的文档（太不像的不要）
                .filterExpression("file_name == '笔记.pdf'")// 过滤：仅检索元数据中文件名等于“笔记.pdf”的文档，用于过滤不相关或内容冲突的文档
                .build();
        List<Document> docs = vectorStore.similaritySearch(request);
        if (docs.isEmpty()) {
            System.out.println("没有搜索到任何内容");
            return;
        }
        for (Document doc : docs) {
            System.out.println(doc.getId());
            System.out.println(doc.getScore());
            System.out.println(doc.getText());
        }
    }
}