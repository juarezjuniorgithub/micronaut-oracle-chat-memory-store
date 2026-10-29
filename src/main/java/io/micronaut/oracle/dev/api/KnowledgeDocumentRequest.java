package io.micronaut.oracle.dev.api;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Serdeable
public record KnowledgeDocumentRequest(@NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 12_000) String content) {
}
