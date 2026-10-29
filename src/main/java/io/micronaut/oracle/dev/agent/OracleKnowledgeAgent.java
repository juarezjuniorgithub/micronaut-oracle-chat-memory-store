package io.micronaut.oracle.dev.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

@AgenticService(tools = OraclePlatformTool.class)
public interface OracleKnowledgeAgent {

  @UserMessage("""
      Answer the following question about Micronaut LangChain4j, Oracle RAG, or durable chat memory.
      Use retrieved knowledge when available and use a tool when it improves architectural accuracy.
      Question: {{question}}
      """)
  @Agent(description = "Answers Oracle-backed LangChain4j architecture questions")
  String answer(@V("question") String question);
}
