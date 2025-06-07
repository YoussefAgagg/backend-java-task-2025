package com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.handler;


import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.response.ApiErrorResponse;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;


/**
 * Handles exceptions of type {@link ObjectOptimisticLockingFailureException} and provides a
 * structured API error response.
 *
 * <p></p>
 * This class is a component that implements the {@link ApiExceptionHandler} interface. Its primary
 * responsibility is to detect whether it can handle a given exception and, if so, to create a
 * customized {@link ApiErrorResponse}.
 *
 * <p></p>
 * The handler is specifically designed to handle optimistic locking failures encountered during
 * database operations, typically when multiple transactions concurrently update the same entity and
 * a conflict arises.
 *
 * <p></p>
 * Responsibilities: - Checks if the exception is an instance of
 * {@link ObjectOptimisticLockingFailureException}. - Constructs an {@link ApiErrorResponse} with
 * proper error details, including: - Error code, key, HTTP status, and message derived from
 * {@link ErrorCode}. - Additional properties such as the identifier and persistent class name of
 * the affected entity.
 *
 * <p></p>
 * This class ensures consistent error responses for optimistic locking issues in API requests,
 * aiding client applications in understanding and addressing the source of the conflict.
 */
@Component
public class ObjectOptimisticLockingFailureApiExceptionHandler implements ApiExceptionHandler {


  @Override
  public boolean canHandle(Throwable exception) {
    return exception instanceof ObjectOptimisticLockingFailureException;
  }

  @Override
  public ApiErrorResponse handle(Throwable exception) {
    var errorCode = ErrorCode.OPTIMISTIC_LOCKING_ERROR;
    ApiErrorResponse response = new ApiErrorResponse(errorCode.getHttpCode(),
                                                     errorCode.getCode(),
                                                     errorCode.name(),
                                                     exception.getMessage(),
                                                     exception.getMessage());
    ObjectOptimisticLockingFailureException ex =
        (ObjectOptimisticLockingFailureException) exception;
    response.addErrorProperty("identifier", ex.getIdentifier());
    response.addErrorProperty("persistentClassName", ex.getPersistentClassName());
    return response;
  }
}
