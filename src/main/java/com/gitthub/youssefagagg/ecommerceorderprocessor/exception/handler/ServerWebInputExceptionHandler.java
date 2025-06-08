package com.gitthub.youssefagagg.ecommerceorderprocessor.exception.handler;


import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.response.ApiErrorResponse;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

/**
 * Exception handler implementation for handling {@link ServerWebInputException}. This handler
 * processes exceptions related to invalid web input, such as issues with request parameters, path
 * variables, or method argument resolution.
 * <br>
 * The handler performs the following: - Determines whether it can handle the exception using
 * {@code canHandle}. - Creates a custom {@link ApiErrorResponse} to represent validation or web
 * input failures. - Populates additional details about the input parameter causing the error, if
 * available.
 * <br>
 * The handler excludes {@link WebExchangeBindException}, which is handled by a different handler
 * (e.g., BindApiExceptionHandler).
 * <br>
 * This class is annotated as a Spring {@link Component} for automatic detection and registration as
 * a bean within the Spring context.
 */
@Component
public class ServerWebInputExceptionHandler implements ApiExceptionHandler {


  @Override
  public boolean canHandle(Throwable exception) {
    return exception instanceof ServerWebInputException
           // WebExchangeBindException should be handled by BindApiExceptionHandler
           && !(exception instanceof WebExchangeBindException);
  }

  @Override
  public ApiErrorResponse handle(Throwable exception) {
    ServerWebInputException ex = (ServerWebInputException) exception;
    var errorCode = ErrorCode.VALIDATION_FAILED;
    ApiErrorResponse response = new ApiErrorResponse(errorCode.getHttpCode(),
                                                     errorCode.getCode(),
                                                     errorCode.name(),
                                                     ex.getMessage(),
                                                     ex.getMessage());
    MethodParameter methodParameter = ex.getMethodParameter();
    if (methodParameter != null) {
      response.addErrorProperty("parameterName", methodParameter.getParameterName());
      response.addErrorProperty("parameterType",
                                methodParameter.getParameterType().getSimpleName());
    }
    return response;
  }
}
