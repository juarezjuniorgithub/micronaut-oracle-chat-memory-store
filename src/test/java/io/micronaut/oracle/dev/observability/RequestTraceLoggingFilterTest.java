package io.micronaut.oracle.dev.observability;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.web.router.RouteAttributes;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestTraceLoggingFilterTest {

  @Test
  void logsTheCompleteHttpLifecycleWithoutReactorTypes() {
    Logger logger = (Logger) LoggerFactory.getLogger(RequestTraceLoggingFilter.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
    try {
      RequestTraceLoggingFilter filter = new RequestTraceLoggingFilter(OpenTelemetry.noop());
      HttpRequest<?> request = HttpRequest.GET("/memory");

      filter.logRequestStart(request);
      filter.logRequestCompletion(request, HttpResponse.ok());

      assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("http.request.start")));
      assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("status=200")));
    } finally {
      logger.detachAppender(appender);
      appender.stop();
    }
  }

  @Test
  void recordsTheOriginalMicronautRouteFailure() {
    Logger logger = (Logger) LoggerFactory.getLogger(RequestTraceLoggingFilter.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
    try {
      RequestTraceLoggingFilter filter = new RequestTraceLoggingFilter(OpenTelemetry.noop());
      HttpRequest<?> request = HttpRequest.GET("/memory");
      MutableHttpResponse<?> response = HttpResponse.serverError();
      RouteAttributes.setException(response, new IllegalStateException("test failure"));

      filter.logRequestStart(request);
      filter.logRequestCompletion(request, response);

      assertTrue(appender.list.stream()
          .anyMatch(event -> event.getFormattedMessage().contains("errorType=IllegalStateException")));
    } finally {
      logger.detachAppender(appender);
      appender.stop();
    }
  }
}
