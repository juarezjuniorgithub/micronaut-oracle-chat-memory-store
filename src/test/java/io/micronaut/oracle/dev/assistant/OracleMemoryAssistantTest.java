package io.micronaut.oracle.dev.assistant;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OracleMemoryAssistantTest {

  private static final Pattern NAME = Pattern.compile("(?i)name is ([A-Za-z]+)");

  @Test
  void reloadsTheSameConversationWindowForANewAssistantInstance() {
    InMemoryChatMemoryStore durableStore = new InMemoryChatMemoryStore();
    ChatModel model = new DeterministicMemoryModel();

    OracleMemoryAssistant firstProcess = newAssistant(durableStore, model);
    var introduction = firstProcess.start("My name is Ada.");
    var sameProcessAnswer = firstProcess.respond(introduction.conversationId(), "What name did I ask you to remember?");

    // A new façade and a new MessageWindow builder simulate an application restart.
    OracleMemoryAssistant restartedProcess = newAssistant(durableStore, model);
    var restartedAnswer = restartedProcess.respond(introduction.conversationId(), "What is my name?");

    assertEquals("Ada", sameProcessAnswer.answer());
    assertEquals("Ada", restartedAnswer.answer());
    assertFalse(durableStore.getMessages(introduction.conversationId()).isEmpty());
  }

  @Test
  void doesNotPersistRetrievedPassagesInTheConversationWindow() {
    InMemoryChatMemoryStore durableStore = new InMemoryChatMemoryStore();
    String retrievedPassage = "RAG corpus text must stay outside durable conversation history.";
    OracleMemoryAssistant assistant = newAssistant(durableStore, new DeterministicMemoryModel(), query -> {
      assertNotNull(query);
      return List.of(Content.from(TextSegment.from(retrievedPassage)));
    });

    var response = assistant.start("Please remember that I prefer concise answers.");
    List<String> persistedUserMessages = durableStore.getMessages(response.conversationId()).stream()
        .filter(UserMessage.class::isInstance).map(UserMessage.class::cast).map(UserMessage::singleText).toList();

    assertEquals(List.of("Please remember that I prefer concise answers."), persistedUserMessages);
    assertTrue(response.retrievedSources().contains(retrievedPassage));
    assertFalse(persistedUserMessages.stream().anyMatch(message -> message.contains(retrievedPassage)));
  }

  private OracleMemoryAssistant newAssistant(InMemoryChatMemoryStore store, ChatModel model) {
    return newAssistant(store, model, query -> {
      assertNotNull(query);
      return List.of();
    });
  }

  private OracleMemoryAssistant newAssistant(InMemoryChatMemoryStore store, ChatModel model,
      ContentRetriever contentRetriever) {
    MessageWindowChatMemory.Builder builder = MessageWindowChatMemory.builder().chatMemoryStore(store).maxMessages(12);
    return new OracleMemoryAssistant(builder, model, contentRetriever);
  }

  private static final class DeterministicMemoryModel implements ChatModel {

    @Override
    public ChatResponse doChat(ChatRequest request) {
      String lastQuestion = UserMessage.findLast(request.messages()).map(UserMessage::singleText).orElseThrow();
      if (lastQuestion.toLowerCase().contains("what") && lastQuestion.toLowerCase().contains("name")) {
        return ChatResponse.builder().aiMessage(AiMessage.from(findRememberedName(request.messages()))).build();
      }
      return ChatResponse.builder().aiMessage(AiMessage.from("Acknowledged")).build();
    }

    private String findRememberedName(List<ChatMessage> messages) {
      return messages.stream().filter(UserMessage.class::isInstance).map(UserMessage.class::cast)
          .map(UserMessage::singleText).map(NAME::matcher).filter(Matcher::find).map(matcher -> matcher.group(1))
          .findFirst().orElse("unknown");
    }
  }
}
