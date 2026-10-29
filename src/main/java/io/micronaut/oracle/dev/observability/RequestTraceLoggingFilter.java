package io.micronaut.oracle.dev.observability;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.annotation.Order;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ResponseFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.web.router.RouteAttributes;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

@ServerFilter(ServerFilter.MATCH_ALL_PATTERN)
@Order(Ordered.LOWEST_PRECEDENCE)
public final class RequestTraceLoggingFilter {

  private static final Logger LOG = LoggerFactory.getLogger(RequestTraceLoggingFilter.class);
  private static final String REQUEST_TRACE_ATTRIBUTE = RequestTraceLoggingFilter.class.getName() + ".trace";
  public static final String TRACE_PARENT_CONTEXT_ATTRIBUTE = RequestTraceLoggingFilter.class.getName()
      + ".parentContext";
  private final Tracer tracer;

  @Inject
  public RequestTraceLoggingFilter(OpenTelemetry openTelemetry) {
    this.tracer = openTelemetry.getTracer("io.micronaut.oracle.dev.http");
  }

  @RequestFilter
  void logRequestStart(HttpRequest<?> request) {
    long startedAt = System.nanoTime();
    Context parentContext = Context.current();
    Span requestSpan = Span.current();
    SpanContext context = requestSpan.getSpanContext();
    boolean ownsSpan = !context.isValid();
    if (ownsSpan) {
      requestSpan = tracer.spanBuilder("http.request").setSpanKind(SpanKind.SERVER)
          .setAttribute("http.request.method", request.getMethodName()).setAttribute("url.path", request.getPath())
          .startSpan();
      context = requestSpan.getSpanContext();
    }
    Context requestContext = requestSpan.storeInContext(parentContext);
    request.setAttribute(TRACE_PARENT_CONTEXT_ATTRIBUTE, requestContext);
    request.setAttribute(REQUEST_TRACE_ATTRIBUTE, new RequestTrace(startedAt, requestSpan, context, ownsSpan));
    LOG.info("http.request.start method={} path={} traceId={} spanId={}", request.getMethod(), request.getPath(),
        traceId(context), spanId(context));
  }

  @ResponseFilter
  void logRequestCompletion(HttpRequest<?> request, HttpResponse<?> response) {
    RequestTrace requestTrace = request.getAttribute(REQUEST_TRACE_ATTRIBUTE, RequestTrace.class).orElse(null);
    if (requestTrace == null || !requestTrace.complete()) {
      return;
    }
    try {
      Throwable failure = RouteAttributes.getException(response).orElse(null);
      requestTrace.span().setAttribute("http.response.status_code", response.code());
      requestTrace.span().setAttribute("http.request.termination", "response");
      if (failure != null) {
        requestTrace.span().recordException(failure);
        requestTrace.span().setStatus(StatusCode.ERROR, failure.getClass().getSimpleName());
        LOG.warn("http.request.failed method={} path={} status={} durationMs={} traceId={} spanId={} errorType={}",
            request.getMethod(), request.getPath(), response.code(), elapsedMillis(requestTrace.startedAt()),
            traceId(requestTrace.context()), spanId(requestTrace.context()), failure.getClass().getSimpleName());
      } else {
        LOG.info("http.request.complete method={} path={} status={} durationMs={} traceId={} spanId={}",
            request.getMethod(), request.getPath(), response.code(), elapsedMillis(requestTrace.startedAt()),
            traceId(requestTrace.context()), spanId(requestTrace.context()));
      }
    } finally {
      if (requestTrace.ownsSpan()) {
        requestTrace.span().end();
      }
    }
  }

  private static long elapsedMillis(long startedAt) {
    return (System.nanoTime() - startedAt) / 1_000_000;
  }

  private static String traceId(SpanContext context) {
    return context.isValid() ? context.getTraceId() : "none";
  }

  private static String spanId(SpanContext context) {
    return context.isValid() ? context.getSpanId() : "none";
  }

  private record RequestTrace(long startedAt, Span span, SpanContext context, boolean ownsSpan,
      AtomicBoolean completed) {

    private RequestTrace(long startedAt, Span span, SpanContext context, boolean ownsSpan) {
      this(startedAt, span, context, ownsSpan, new AtomicBoolean());
    }

    private boolean complete() {
      return completed.compareAndSet(false, true);
    }
  }
}
