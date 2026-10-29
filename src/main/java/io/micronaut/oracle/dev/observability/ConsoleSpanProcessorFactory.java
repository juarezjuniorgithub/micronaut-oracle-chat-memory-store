package io.micronaut.oracle.dev.observability;

import io.micronaut.context.annotation.Factory;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import jakarta.inject.Singleton;

@Factory
public final class ConsoleSpanProcessorFactory {

  @Singleton
  SpanProcessor consoleSpanProcessor() {
    return SimpleSpanProcessor.create(LoggingSpanExporter.create());
  }
}
