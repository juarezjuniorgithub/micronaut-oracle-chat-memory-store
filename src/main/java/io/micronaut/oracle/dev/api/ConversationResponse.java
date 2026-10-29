package io.micronaut.oracle.dev.api;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record ConversationResponse(String conversationId, String answer, List<String> retrievedSources) {
}
