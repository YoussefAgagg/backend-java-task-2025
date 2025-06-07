package com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.handler;


import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.response.ApiErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * Implementation of the {@link ApiExceptionHandler} interface used to handle specific exceptions of
 * type {@link CustomException}. This class provides mechanisms to inspect an exception and generate
 * a structured error response for the API layer.
 *
 * <p>This handler is a Spring component and works in conjunction with the
 * {@link MessageSource} to potentially enable localization of error messages (though not directly
 * used in this implementation). Using the injected {@link MessageSource}, messages can be retrieved
 * and formatted based on the locale in more advanced implementations.</p>
 *
 * <p></p>
 * Features: - Determines if it can handle the exception by checking if it is an instance of
 * {@link CustomException}. - Converts the {@link CustomException} to a structured
 * {@link ApiErrorResponse} object containing relevant error details such as HTTP status code, error
 * code, error name, default messages, and a detailed error message.
 */
@Component
@RequiredArgsConstructor
public class BaseExceptionHandler implements ApiExceptionHandler {


  @Override
  public boolean canHandle(Throwable exception) {
    return exception instanceof CustomException;
  }

  @Override
  public ApiErrorResponse handle(Throwable exception) {
    var ex = (CustomException) exception;
    var errorCode = ex.getErrorCode();
    return new ApiErrorResponse(errorCode.getHttpCode(),
                                errorCode.getCode(),
                                errorCode.name(),
                                errorCode.getMessage(),
                                ex.getMessage());
  }
}
