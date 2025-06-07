package com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.response;

/**
 * Represents a field-specific error within an API response. This record is primarily used to
 * encapsulate details about validation errors or other issues related to specific properties or
 * fields in a request payload. It is often included in an {@code ApiErrorResponse} to provide more
 * granular context about the errors encountered during the processing of a request. The record
 * contains the following attributes: - {@code property}: The name of the property or field on which
 * the error occurred. - {@code message}: A descriptive message explaining the nature of the error.
 * - {@code rejectedValue}: The value of the property that was rejected or caused the error. -
 * {@code path}: The navigation path to the property or field in a complex object structure, if
 * applicable.
 */
public record ApiFieldError(String property,
                            String message,
                            Object rejectedValue,
                            String path) {
}
