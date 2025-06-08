package com.gitthub.youssefagagg.ecommerceorderprocessor.exception.handler;


import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.response.ApiErrorResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;

/**
 * {@link ApiExceptionHandler} for {@link HttpMessageNotReadableException}. This typically happens
 * when Spring can't properly decode the incoming request to JSON.
 */
@Component
public class HttpMessageNotReadableApiExceptionHandler implements ApiExceptionHandler {


  @Override
  public boolean canHandle(Throwable exception) {
    return exception instanceof HttpMessageNotReadableException;
  }

  @Override
  public ApiErrorResponse handle(Throwable exception) {
    var httpMessageNotReadable = ErrorCode.HTTP_MESSAGE_NOT_READABLE;
    return new ApiErrorResponse(httpMessageNotReadable.getHttpCode(),
                                httpMessageNotReadable.getCode(),
                                httpMessageNotReadable.name(),
                                exception.getMessage(),
                                exception.getMessage());
  }

}
