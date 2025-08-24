package com.xuenai.assistant.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 加载 RAG
 */
@Configuration
public class RAGConfig {
    
    @Resource
    private EmbeddingModel qwenEmbeddingModel;
    
    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;
    
    @Bean
    public ContentRetriever contentRetriever() {
//        加载文档
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs");
//        文档切割，将每一个文档按段进行切割，最大 1000 字符，且每次最多重叠 200 字符
        DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(1000, 200);
//        自定义文档加载器
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .textSegmentTransformer(textSegment -> TextSegment.from(
                        // 创建一个新的 TextSegment，使用 文件名 与 原文件中的内容做拼接
                        //file_name：源文件名。
                        //source：文件路径或URI。
                        //page_number：如果来自PDF，是哪一页。
                        //segment_index：该段落在文档中的序号。
                        textSegment.metadata().getString("file_name") + "\n" + textSegment.text(),
                        // 与该文本片段相关的元数据（附加信息）
                        textSegment.metadata()
                ))
                // 指定使用向量模型
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .build();
//        加载文档
        ingestor.ingest(documents);
//        自定义内容查询器
        EmbeddingStoreContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(5) // 最多5个检索结构
                .minScore(0.75) // 评分需要在 0.75 之上
                .build();
        return retriever;
    }
    
}
