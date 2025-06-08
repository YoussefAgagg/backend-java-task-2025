package com.gitthub.youssefagagg.ecommerceorderprocessor.exception.handler;


import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.response.ApiErrorResponse;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingMatrixVariableException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MissingServletRequestParameterException;

/**
 * A handler for managing exceptions related to missing request values in an API context. This
 * handler implements the {@link ApiExceptionHandler} interface and is responsible for identifying
 * and processing exceptions such as missing matrix variables, path variables, request headers,
 * request cookies, or servlet request parameters.
 *
 * <p></p>
 * The handler will first determine whether it can handle a specific exception by implementing the
 * {@link #canHandle(Throwable)} method. If the exception is determined to be supported, the
 * {@link #handle(Throwable)} method is invoked to produce a structured {@link ApiErrorResponse}.
 *
 * <p></p>
 * For each type of missing request value exception, the handler provides additional details about
 * the error, such as the missing variable name or parameter type. This information is encapsulated
 * within the generated {@link ApiErrorResponse}.
 *
 * <p></p>
 * The exceptions currently handled include: - {@link MissingMatrixVariableException}: Missing
 * matrix variable in the request. - {@link MissingPathVariableException}: Missing path variable in
 * the request. - {@link MissingRequestCookieException}: Missing cookie in the request. -
 * {@link MissingRequestHeaderException}: Missing header in the request. -
 * {@link MissingServletRequestParameterException}: Missing request parameter in the servlet.
 */
@Component
public class MissingRequestValueExceptionHandler implements ApiExceptionHandler {


  @Override
  public boolean canHandle(Throwable exception) {
    return exception instanceof MissingRequestValueException;
  }

  @Override
  public ApiErrorResponse handle(Throwable exception) {
    var errorCode = ErrorCode.VALIDATION_FAILED;
    ApiErrorResponse response = new ApiErrorResponse(errorCode.getHttpCode(),
                                                     errorCode.getCode(),
                                                     errorCode.name(),
                                                     exception.getMessage(),
                                                     exception.getMessage());
    switch (exception) {
      case MissingMatrixVariableException missingMatrixVariableException -> {
        response.addErrorProperty("variableName",
                                  missingMatrixVariableException.getVariableName());
        addParameterInfo(response, missingMatrixVariableException.getParameter());
      }
      case MissingPathVariableException missingPathVariableException -> {
        response.addErrorProperty("variableName",
                                  missingPathVariableException.getVariableName());
        addParameterInfo(response, missingPathVariableException.getParameter());
      }
      case MissingRequestCookieException missingRequestCookieException -> {
        response.addErrorProperty("cookieName",
                                  missingRequestCookieException.getCookieName());
        addParameterInfo(response, missingRequestCookieException.getParameter());
      }
      case MissingRequestHeaderException missingRequestHeaderException -> {
        response.addErrorProperty("headerName",
                                  missingRequestHeaderException.getHeaderName());
        addParameterInfo(response, missingRequestHeaderException.getParameter());
      }
      case MissingServletRequestParameterException missingServletRequestParameterException -> {
        String parameterName =
            missingServletRequestParameterException.getParameterName();
        String parameterType =
            missingServletRequestParameterException.getParameterType();
        response.addErrorProperty("parameterName", parameterName);
        response.addErrorProperty("parameterType", parameterType);
      }
      default -> {
      }
    }
    return response;
  }

  private void addParameterInfo(ApiErrorResponse response, MethodParameter parameter) {
    response.addErrorProperty("parameterName", parameter.getParameterName());
    response.addErrorProperty("parameterType", parameter.getParameterType().getSimpleName());
  }
}
