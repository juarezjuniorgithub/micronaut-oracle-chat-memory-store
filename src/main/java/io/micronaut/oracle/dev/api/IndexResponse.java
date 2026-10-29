package io.micronaut.oracle.dev.api;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record IndexResponse(String documentId, boolean seeded) {
}
