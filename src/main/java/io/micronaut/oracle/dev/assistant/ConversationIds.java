package io.micronaut.oracle.dev.assistant;

import java.util.UUID;

public final class ConversationIds {

  private ConversationIds() {
  }

  public static String newId() {
    return UUID.randomUUID().toString();
  }

  public static String requireValid(String conversationId) {
    try {
      return UUID.fromString(conversationId).toString();
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("conversationId must be a UUID", exception);
    }
  }
}
