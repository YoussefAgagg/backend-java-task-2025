package com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.handler;


import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.response.ApiErrorResponse;

/**
 * Interface for handling exceptions in the API layer. Implementers of this interface are
 * responsible for determining whether they can handle a given exception and for generating an
 * appropriate API error response.
 */
public interface ApiExceptionHandler {
  /**
   * Determine if this {@link ApiExceptionHandler} can handle the given {@link Throwable}. It is
   * guaranteed that this method is called first, and the {@link #handle(Throwable)} method will
   * only be called if this method returns <code>true</code>.
   *
   * @param exception the Throwable that needs to be handled
   * @return true if this handler can handle the Throwable, false otherwise.
   */
  boolean canHandle(Throwable exception);

  /**
   * Handle the given {@link Throwable} and return an {@link ApiErrorResponse} instance that will be
   * serialized to JSON and returned from the controller method that has thrown the Throwable.
   *
   * @param exception the Throwable that needs to be handled
   * @return the non-null ApiErrorResponse
   */
  ApiErrorResponse handle(Throwable exception);
}
