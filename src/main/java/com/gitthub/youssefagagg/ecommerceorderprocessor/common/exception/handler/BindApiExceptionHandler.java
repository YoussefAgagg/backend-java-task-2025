package com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.handler;


import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.response.ApiErrorResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.response.ApiFieldError;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.response.ApiGlobalError;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Class to handle {@link BindException} and {@link MethodArgumentNotValidException} exceptions.
 * This is typically used: * when `@Valid` is used on
 * {@link org.springframework.web.bind.annotation.RestController} method arguments. * when `@Valid`
 * is used on {@link org.springframework.web.bind.annotation.RestController} query parameters
 */
@Component
public class BindApiExceptionHandler implements
    ApiExceptionHandler {


  @Override
  public boolean canHandle(Throwable exception) {
    // BindingResult is a common interface between org.springframework.validation.BindException
    // and org.springframework.web.bind.support.WebExchangeBindException
    return exception instanceof BindingResult;
  }

  @Override
  public ApiErrorResponse handle(Throwable exception) {

    BindingResult bindingResult = (BindingResult) exception;
    var validationFailed = ErrorCode.VALIDATION_FAILED;
    ApiErrorResponse response =
        new ApiErrorResponse(validationFailed.getHttpCode(),
                             validationFailed.getCode(),
                             validationFailed.name(),
                             getMessage(bindingResult),
                             getMessage(bindingResult));
    if (bindingResult.hasFieldErrors()) {
      bindingResult.getFieldErrors().stream()
                   .map(fieldError -> new ApiFieldError(
                       fieldError.getField(),
                       fieldError.getDefaultMessage(),
                       fieldError.getRejectedValue(),
                       getPath(fieldError)))
                   .forEach(response::addFieldError);
    }

    if (bindingResult.hasGlobalErrors()) {
      bindingResult.getGlobalErrors().stream()
                   .map(globalError -> new ApiGlobalError(globalError.getDefaultMessage()))
                   .forEach(response::addGlobalError);
    }

    return response;
  }

  private String getMessage(BindingResult bindingResult) {
    return "Validation failed for object='" + bindingResult.getObjectName() + "'. Error count: "
           + bindingResult.getErrorCount();
  }

  private String getPath(FieldError fieldError) {

    String path = null;
    try {
      path = fieldError.unwrap(ConstraintViolationImpl.class)
                       .getPropertyPath()
                       .toString();
    } catch (RuntimeException runtimeException) {
      // only set a path if we have a ConstraintViolation
    }
    return path;
  }
}
