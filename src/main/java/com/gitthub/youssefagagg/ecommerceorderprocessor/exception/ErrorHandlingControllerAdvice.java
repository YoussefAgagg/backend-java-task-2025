package com.gitthub.youssefagagg.ecommerceorderprocessor.exception;


import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.handler.ApiExceptionHandler;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.response.ApiErrorResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.TracerService;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * A global exception handler for handling exceptions in Spring Boot applications. This class
 * integrates multiple {@link ApiExceptionHandler} implementations to provide a flexible and modular
 * approach for error handling. It uses Spring's {@code @RestControllerAdvice} to intercept
 * exceptions thrown from controllers and handle them accordingly.
 *
 * <p></p>
 * Key features include: - Delegation of exception handling to a list of {@link ApiExceptionHandler}
 * instances. - Logging of exception details for debugging and monitoring. - Providing a
 * standardized JSON-based error response using the {@link ApiErrorResponse} class. - Fallback
 * handling for exceptions that are not specifically handled by any registered handler. -
 * Integration of tracing information using the {@link TracerService}.
 *
 * <p></p>
 * Constructor Details: The constructor expects a list of {@link ApiExceptionHandler}
 * implementations and a {@link TracerService}. It sorts the handlers based on their order, allowing
 * prioritized handling.
 *
 * <p></p>
 * Exception Handling: The {@link #handleException(Throwable, WebRequest, Locale)} method intercepts
 * exceptions, logs relevant details, and delegates handling to the appropriate
 * {@link ApiExceptionHandler}. If no handler can handle the exception, a default
 * {@link ApiErrorResponse} is used to respond with a generic error message. The response also
 * includes trace information for debugging.
 */
@RestControllerAdvice
@Slf4j
public class ErrorHandlingControllerAdvice {

  private final List<ApiExceptionHandler> handlers;
  private final TracerService tracer;


  /**
   * Constructs an instance of ErrorHandlingControllerAdvice with the provided list of handlers and
   * a tracer service. It initializes the handlers list, sorts them using
   * AnnotationAwareOrderComparator, and logs the initialization details.
   *
   * @param handlers the list of ApiExceptionHandler instances to handle various API exceptions
   * @param tracer   the TracerService instance used for tracing and monitoring
   */
  public ErrorHandlingControllerAdvice(List<ApiExceptionHandler> handlers,
                                       TracerService tracer) {
    this.handlers = handlers;
    this.tracer = tracer;
    this.handlers.sort(AnnotationAwareOrderComparator.INSTANCE);

    log.info("Error Handling Spring Boot Starter active with {} handlers", this.handlers.size());
    log.debug("Handlers: {}", this.handlers);
  }

  /**
   * Handles exceptions thrown during the execution of an application. Identifies the appropriate
   * handler for specific exceptions, processes the exception, and constructs an error response.
   *
   * @param exception  the throwable instance that represents the exception encountered
   * @param webRequest the web request during which the exception occurred
   * @param locale     the locale information to customize the error response, if applicable
   * @return a {@code ResponseEntity} containing the constructed {@code ApiErrorResponse} object
   */
  @ExceptionHandler
  public ResponseEntity<ApiErrorResponse> handleException(Throwable exception,
                                                          WebRequest webRequest,
                                           Locale locale) {
    log.debug("webRequest: {}", webRequest);
    log.debug("locale: {}", locale);
    log.error(
        "Exception with cause = '{}' and exception = '{}', e=",
        exception.getCause() != null ? exception.getCause() : "NULL",
        exception.getMessage(),
        exception);

    ApiErrorResponse errorResponse = null;
    for (ApiExceptionHandler handler : handlers) {
      if (handler.canHandle(exception)) {
        errorResponse = handler.handle(exception);
        break;
      }
    }

    if (errorResponse == null) {
      var errorCode = ErrorCode.GLOBAL_ERROR;

      errorResponse = new ApiErrorResponse(errorCode.getHttpCode(),
                                           errorCode.getCode(),
                                           errorCode.name(),
                                           errorCode.getMessage(),
                                           exception.getMessage());
    }
    errorResponse.setTraceParent(tracer.getTraceParent());
    return ResponseEntity.status(errorResponse.getHttpStatus())
                         .body(errorResponse);
  }


}
