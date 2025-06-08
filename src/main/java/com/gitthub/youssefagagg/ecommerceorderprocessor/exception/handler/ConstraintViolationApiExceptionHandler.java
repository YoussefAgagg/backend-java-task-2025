package com.gitthub.youssefagagg.ecommerceorderprocessor.exception.handler;

import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.response.ApiErrorResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.response.ApiFieldError;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.response.ApiGlobalError;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.response.ApiParameterError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * {@link ApiExceptionHandler} for {@link ConstraintViolationException}. This typically happens when
 * there is validation on Spring services that gets triggered.
 *
 * @see BindApiExceptionHandler
 */
@Component
public class ConstraintViolationApiExceptionHandler implements ApiExceptionHandler {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ConstraintViolationApiExceptionHandler.class);

  @Override
  public boolean canHandle(Throwable exception) {
    return exception instanceof ConstraintViolationException;
  }

  @Override
  public ApiErrorResponse handle(Throwable exception) {

    ConstraintViolationException ex = (ConstraintViolationException) exception;
    var validationFailed = ErrorCode.VALIDATION_FAILED;
    ApiErrorResponse response = new ApiErrorResponse(validationFailed.getHttpCode(),
                                                     validationFailed.getCode(),
                                                     validationFailed.name(),
                                                     getMessage(ex),
                                                     getMessage(ex));
    Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
    violations.stream()
              // sort violations to ensure deterministic order
              .sorted(Comparator.comparing(
                  constraintViolation -> constraintViolation.getPropertyPath().toString()))
              .map(constraintViolation -> {
                Optional<Path.Node> leafNode = getLeafNode(constraintViolation.getPropertyPath());
                if (leafNode.isPresent()) {
                  Path.Node node = leafNode.get();
                  ElementKind elementKind = node.getKind();
                  if (elementKind == ElementKind.PROPERTY) {
                    return new ApiFieldError(
                        node.toString(),
                        getMessage(constraintViolation),
                        constraintViolation.getInvalidValue(),
                        getPath(constraintViolation));
                  } else if (elementKind == ElementKind.BEAN) {
                    return new ApiGlobalError(getMessage(constraintViolation));
                  } else if (elementKind == ElementKind.PARAMETER) {
                    return new ApiParameterError(
                        node.toString(),
                        getMessage(constraintViolation),
                        constraintViolation.getInvalidValue());
                  } else {
                    LOGGER.warn("Unable to convert constraint violation with element kind {}: {}",
                                elementKind, constraintViolation);
                    return null;
                  }
                } else {
                  LOGGER.warn("Unable to convert constraint violation: {}", constraintViolation);
                  return null;
                }
              })
              .forEach(error -> {
                if (error instanceof ApiFieldError apiFieldError) {
                  response.addFieldError(apiFieldError);
                } else if (error instanceof ApiGlobalError apiGlobalError) {
                  response.addGlobalError(apiGlobalError);
                } else if (error instanceof ApiParameterError apiParameterError) {
                  response.addParameterError(apiParameterError);
                }
              });

    return response;
  }

  private Optional<Path.Node> getLeafNode(Path path) {
    return StreamSupport.stream(path.spliterator(), false).reduce((a, b) -> b);
  }

  private String getPath(ConstraintViolation<?> constraintViolation) {

    return getPathWithoutPrefix(constraintViolation.getPropertyPath());
  }

  /**
   * This method will truncate the first 2 parts of the full property path so the method name and
   * argument name are not visible in the returned path.
   *
   * @param path the full property path of the constraint violation
   * @return The truncated property path
   */
  private String getPathWithoutPrefix(Path path) {
    String collect = StreamSupport.stream(path.spliterator(), false)
                                  .limit(2)
                                  .map(Path.Node::getName)
                                  .collect(Collectors.joining("."));
    String substring = path.toString().substring(collect.length());
    return substring.startsWith(".") ? substring.substring(1) : substring;
  }


  private String getMessage(ConstraintViolation<?> constraintViolation) {
    return constraintViolation.getMessage();
  }

  private String getMessage(ConstraintViolationException exception) {
    return "Validation failed. Error count: " + exception.getConstraintViolations().size();
  }
}
