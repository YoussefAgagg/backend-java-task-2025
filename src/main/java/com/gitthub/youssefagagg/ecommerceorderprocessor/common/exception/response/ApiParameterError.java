package com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.response;

/**
 * Represents a parameter-specific error within an API response.
 *
 * <p>This record is designed to encapsulate information about errors that occur
 * in relation to specific parameters in an API request. It serves to provide detailed feedback
 * about invalid or problematic parameters encountered during the request validation or processing
 * phase.
 * </p>
 * The record contains the following attributes: - {@code parameter}: The name of the parameter
 * where the error occurred. - {@code message}: A descriptive message that details the nature of the
 * error. - {@code rejectedValue}: The value of the parameter that was rejected or caused the
 * error.
 */
public record ApiParameterError(String parameter,
                                String message,
                                Object rejectedValue) {
}
