package com.gitthub.youssefagagg.ecommerceorderprocessor.exception.handler;


import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.response.ApiErrorResponse;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerErrorException;

/**
 * Handles exceptions of type {@link ServerErrorException} and generates an instance of
 * {@link ApiErrorResponse} to be returned to the client. This handler retrieves specific details
 * about the exception, such as the method parameter and handler method information, and includes
 * them in the error response.
 * <br>
 * This class is a {@link Component}, allowing it to be discovered and managed by the Spring
 * framework.
 * <br>
 * This class implements the {@link ApiExceptionHandler} interface, which allows the determination
 * of whether a given exception can be handled and the creation of an appropriate error response.
 * <br>
 * Constructor injection is used to provide the necessary dependencies.
 */
@Component
@RequiredArgsConstructor
public class ServerErrorExceptionHandler implements ApiExceptionHandler {


  @Override
  public boolean canHandle(Throwable exception) {
    return exception instanceof ServerErrorException;
  }

  @Override
  public ApiErrorResponse handle(Throwable exception) {
    ServerErrorException ex = (ServerErrorException) exception;
    var errorCode = ErrorCode.GLOBAL_ERROR;
    ApiErrorResponse response = new ApiErrorResponse(errorCode.getHttpCode(),
                                                     errorCode.getCode(),
                                                     errorCode.name(),
                                                     errorCode.getMessage());
    MethodParameter methodParameter = ex.getMethodParameter();
    if (methodParameter != null) {
      response.addErrorProperty("parameterName", methodParameter.getParameterName());
      response.addErrorProperty("parameterType",
                                methodParameter.getParameterType().getSimpleName());
    }

    Method handlerMethod = ex.getHandlerMethod();
    if (handlerMethod != null) {
      response.addErrorProperty("methodName", handlerMethod.getName());
      response.addErrorProperty("methodClassName",
                                handlerMethod.getDeclaringClass().getSimpleName());
    }

    return response;
  }
}
