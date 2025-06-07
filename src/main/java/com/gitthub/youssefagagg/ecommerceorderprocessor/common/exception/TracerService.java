package com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception;


import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TracerService is a service that interacts with the Tracer component to fetch trace information.
 * It includes functionality to retrieve the trace parent string, adhering to the W3C trace-context
 * standard. The service leverages the current active span within the tracing context to build the
 * trace parent string.
 */
@Component
@RequiredArgsConstructor
public class TracerService {
  private final Tracer tracer;

  /**
   * Generates the trace parent string for the current active span in compliance with the W3C
   * trace-context standard. The trace parent string includes the trace version, trace ID, span ID,
   * and a sampled flag. If no active span is present, a default value representing a trace with all
   * fields zeroed is returned.
   *
   * @return A string representing the trace parent for the current span or a default value if no
   *     span is active.
   */
  public String getTraceParent() {
    Span span = tracer.currentSpan();
    if (span != null) {
      String sampledFlag = Boolean.TRUE.equals(span.context().sampled()) ? "01" : "00";
      return "00-%s-%s-%s".formatted(span.context().traceId(), span.context().spanId(),
                                     sampledFlag);
    } else {
      return "00-00000000000000000000000000000000-0000000000000000-00";
    }
  }
}
