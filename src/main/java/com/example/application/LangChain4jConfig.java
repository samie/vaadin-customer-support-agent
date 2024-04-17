package com.example.application;

import com.example.application.services.BookingTools;
import com.example.application.services.CustomerSupportAgent;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.retriever.Retriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import static dev.langchain4j.data.document.splitter.DocumentSplitters.recursive;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4;

@Configuration
public class LangChain4jConfig {

    @Bean
    EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    Tokenizer tokenizer() {
        return new OpenAiTokenizer(GPT_4);
    }

    // In the real world, ingesting documents would often happen separately, on a CI server or similar
    @Bean
    CommandLineRunner ingestDocsForLangChain(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            Tokenizer tokenizer, // Tokenizer is provided by langchain4j-open-ai-spring-boot-starter
            ResourceLoader resourceLoader
    ) {
        return args -> {
            var resource = resourceLoader.getResource("classpath:terms-of-service.txt");
            var termsOfUse = loadDocument(resource.getFile().toPath(), new TextDocumentParser());
            var ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(recursive(50, 0, tokenizer))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();
            ingestor.ingest(termsOfUse);
        };
    }

    @Bean
    ContentRetriever contentRetriever(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel
    ) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();
    }

    @Bean
    ChatMemoryProvider chatMemoryProvider(Tokenizer tokenizer) {
        // Tokenizer is provided by langchain4j-open-ai-spring-boot-starter
        return chatId -> TokenWindowChatMemory.withMaxTokens(1000, tokenizer);
    }

    @Bean
    StreamingChatLanguageModel chatLanguageModel(@Value("${openai.api.key}") String apiKey) {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(GPT_4)
                .build();
    }
    @Bean
    Retriever<TextSegment> retriever(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel
    ) {
        return EmbeddingStoreRetriever.from(
                embeddingStore,
                embeddingModel,
                1,
                0.6);
    }
    @Bean
    CustomerSupportAgent customerSupportAgent(
            StreamingChatLanguageModel chatLanguageModel,
            Tokenizer tokenizer,
            Retriever<TextSegment> retriever,
            BookingTools tools
    ) {

        return AiServices.builder(CustomerSupportAgent.class)
                .streamingChatLanguageModel(chatLanguageModel)
                .chatMemoryProvider(chatId -> TokenWindowChatMemory.builder()
                        .id(chatId)
                        .maxTokens(500, tokenizer)
                        .build())
                .retriever(retriever)
                .tools(tools)
                .build();
    }
}