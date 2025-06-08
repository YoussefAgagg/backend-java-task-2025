package com.gitthub.youssefagagg.ecommerceorderprocessor.exception.handler;


import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.response.ApiErrorResponse;
import org.springframework.beans.TypeMismatchException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Handles exceptions of type {@link TypeMismatchException} and generates an appropriate
 * {@link ApiErrorResponse} for the API client. This exception handler processes errors related to
 * type mismatches when binding request parameters or method arguments, providing detailed
 * information about the issue.
 *
 * <p></p>
 * This handler is capable of determining whether it can handle a given exception and constructing a
 * response that includes details about the mismatched property, the rejected value, and the
 * expected type.
 */
@Component
public class TypeMismatchApiExceptionHandler implements ApiExceptionHandler {


  @Override
  public boolean canHandle(Throwable exception) {
    return exception instanceof TypeMismatchException;
  }

  @Override
  public ApiErrorResponse handle(Throwable exception) {
    var typeMismatch = ErrorCode.TYPE_MISMATCH;
    ApiErrorResponse response = new ApiErrorResponse(typeMismatch.getHttpCode(),
                                                     typeMismatch.getCode(),
                                                     typeMismatch.name(),
                                                     exception.getMessage(),
                                                     exception.getMessage());
    TypeMismatchException ex = (TypeMismatchException) exception;
    response.addErrorProperty("property", getPropertyName(ex));
    response.addErrorProperty("rejectedValue", ex.getValue());
    response.addErrorProperty("expectedType",
                              ex.getRequiredType() != null ? ex.getRequiredType().getName() : null);
    return response;
  }

  private String getPropertyName(TypeMismatchException exception) {
    if (exception instanceof MethodArgumentTypeMismatchException) {
      return ((MethodArgumentTypeMismatchException) exception).getName();
    } else {
      return exception.getPropertyName();
    }
  }
}
