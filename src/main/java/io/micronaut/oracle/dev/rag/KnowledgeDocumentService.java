package io.micronaut.oracle.dev.rag;

import io.micronaut.oracle.dev.api.IndexResponse;
import io.micronaut.oracle.dev.api.KnowledgeDocumentRequest;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@Singleton
public class KnowledgeDocumentService {

  private static final Logger LOG = LoggerFactory.getLogger(KnowledgeDocumentService.class);

  private final EmbeddingStore<TextSegment> embeddingStore;
  private final EmbeddingModel embeddingModel;

  public KnowledgeDocumentService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
    this.embeddingStore = Objects.requireNonNull(embeddingStore, "embeddingStore");
    this.embeddingModel = Objects.requireNonNull(embeddingModel, "embeddingModel");
  }

  public IndexResponse index(KnowledgeDocumentRequest request) {
    return index(request, false);
  }

  private IndexResponse index(KnowledgeDocumentRequest request, boolean seeded) {
    long startedAt = System.nanoTime();
    LOG.info("rag.index.start seeded={} titleCharacters={} contentCharacters={}", seeded, request.title().length(),
        request.content().length());
    TextSegment segment = TextSegment.from("Title: " + request.title().trim() + "\n\n" + request.content().trim(),
        Metadata.from("title", request.title().trim()));
    try {
      String id = embeddingStore.add(embeddingModel.embed(segment).content(), segment);
      LOG.info("rag.index.complete seeded={} embeddingId={} durationMs={}", seeded, id, elapsedMillis(startedAt));
      return new IndexResponse(id, seeded);
    } catch (RuntimeException exception) {
      // Deliberately omit title/content: knowledge data can be sensitive.
      LOG.warn("rag.index.failed seeded={} durationMs={} errorType={}", seeded, elapsedMillis(startedAt),
          exception.getClass().getSimpleName());
      throw exception;
    }
  }

  public List<IndexResponse> seedDemoCorpus() {
    LOG.info("rag.demo-corpus.seed.start documentCount=3");
    return List.of(
        index(new KnowledgeDocumentRequest("Oracle chat memory",
            "OracleChatMemoryStore persists LangChain4j conversation messages in "
                + "CHAT_MEMORY. The same memory id reloads the complete stored window."),
            true),
        index(new KnowledgeDocumentRequest("Oracle RAG",
            "Oracle AI Vector Search stores document embeddings in DOCUMENT_EMBEDDINGS. "
                + "A ContentRetriever embeds a question, finds relevant segments, and supplies "
                + "those segments to the chat model."),
            true),
        index(new KnowledgeDocumentRequest("Agent memory boundary",
            "Never use a shared agent-memory id for multiple users. Bind one opaque conversation "
                + "id to the authenticated subject and enforce that ownership before loading memory."),
            true));
  }

  private static long elapsedMillis(long startedAt) {
    return (System.nanoTime() - startedAt) / 1_000_000;
  }
}
