package io.micronaut.oracle.dev.assistant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConversationIdsTest {

  @Test
  void acceptsGeneratedUuid() {
    assertDoesNotThrow(() -> ConversationIds.requireValid(ConversationIds.newId()));
  }

  @Test
  void rejectsAValueThatCannotBeAConversationKey() {
    assertThrows(IllegalArgumentException.class, () -> ConversationIds.requireValid("not-a-database-key"));
  }
}
