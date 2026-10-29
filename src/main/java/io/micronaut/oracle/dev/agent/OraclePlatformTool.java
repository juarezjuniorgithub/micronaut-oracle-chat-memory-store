package io.micronaut.oracle.dev.agent;

import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;

@Singleton
public class OraclePlatformTool {

  @Tool("Returns the architecture of this sample's persistent-memory workflow")
  public String persistentMemoryArchitecture() {
    return "A MessageWindowChatMemory is rebuilt for each UUID conversation id; "
        + "OracleChatMemoryStore persists that window in CHAT_MEMORY; "
        + "Oracle AI Vector Search retrieves RAG context from DOCUMENT_EMBEDDINGS.";
  }
}
