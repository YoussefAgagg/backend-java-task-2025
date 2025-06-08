package com.gitthub.youssefagagg.ecommerceorderprocessor.exception.handler;


import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.response.ApiErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Handles exceptions of type {@link NoHandlerFoundException} by implementing the
 * {@link ApiExceptionHandler} interface. This is primarily used for handling cases where a client
 * makes a request to an endpoint that does not exist within the application.
 *
 * <p></p>
 * This handler: - Checks if the thrown exception is an instance of {@link NoHandlerFoundException}.
 * - Creates and returns an {@link ApiErrorResponse} encapsulating the error information. - Provides
 * detailed contextual information about the missing endpoint including the HTTP method and the
 * request URL.
 */
@Component
@RequiredArgsConstructor
public class NoHandlerFoundExceptionHandler implements ApiExceptionHandler {


  @Override
  public boolean canHandle(Throwable exception) {
    return exception instanceof NoHandlerFoundException;
  }

  @Override
  @SuppressWarnings("checkstyle:linelength")
  public ApiErrorResponse handle(Throwable exception) {
    NoHandlerFoundException ex = (NoHandlerFoundException) exception;
    var errorCode = ErrorCode.ENDPOINT_NOT_FOUND;
    return new ApiErrorResponse(errorCode.getHttpCode(),
                                errorCode.getCode(),
                                errorCode.name(),
                                String.format(
                                    "The endpoint %s '%s' does not exist. Please check the URL and try again.",
                                    ex.getHttpMethod(),
                                    ex.getRequestURL()));
  }
}
