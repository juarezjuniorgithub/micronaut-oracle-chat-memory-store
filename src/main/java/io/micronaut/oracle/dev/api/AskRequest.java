package io.micronaut.oracle.dev.api;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Serdeable
public record AskRequest(@NotBlank @Size(max = 4_000) String message) {
}
