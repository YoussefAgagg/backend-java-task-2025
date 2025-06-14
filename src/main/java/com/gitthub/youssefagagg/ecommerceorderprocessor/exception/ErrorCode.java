package com.gitthub.youssefagagg.ecommerceorderprocessor.exception;


import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Enumeration representing various error codes, messages, and HTTP statuses for different types of
 * errors encountered in the application. This enum is structured to provide a standardized way of
 * handling error codes and messages for various error scenarios, both predefined and custom.
 *
 * <p></p>
 * Categories of errors defined include: - Spring framework-related errors - Security-related errors
 * - Conflict errors - Global generic errors - Custom domain-specific errors for User, Product,
 * Vault, Customer, Supplier, Payment, Sale, Purchase, Expense, Revenue, Asset, Loan, and Partner
 * modules.
 *
 * <p></p>
 * Each enumeration value holds: - A unique error code represented as a String - A message key that
 * can be used for localization or descriptive purposes - An associated HTTP status code to suggest
 * the HTTP response status
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // spring errors
  VALIDATION_FAILED("400001",
                    "error.validation.failed",
                    BAD_REQUEST),
  HTTP_MESSAGE_NOT_READABLE("400002",
                            "error.http.message.not.readable",
                            BAD_REQUEST),
  TYPE_MISMATCH("400003",
                "error.type.mismatch",
                BAD_REQUEST),
  FILE_SIZE_EXCEEDED("400004",
                     "error.file.size.exceeded",
                     BAD_REQUEST),
  MULTIPART_EXCEPTION("400005",
                      "error.multipart.exception",
                      BAD_REQUEST),
  ENDPOINT_NOT_FOUND("404001",
                     "error.endpoint.not.found",
                     NOT_FOUND),

  // security errors
  ACCESS_DENIED("401001",
                "error.access.denied",
                FORBIDDEN),
  ACCOUNT_EXPIRED("401002",
                  "error.account.expired",
                  BAD_REQUEST),
  AUTHENTICATION_CREDENTIALS_NOT_FOUND("401003",
                                       "error.authentication.credentials.not.found",
                                       UNAUTHORIZED),
  AUTHENTICATION_SERVICE_EXCEPTION("401004",
                                   "error.authentication.service.exception",
                                   INTERNAL_SERVER_ERROR),
  BAD_CREDENTIALS("401005",
                  "error.bad.credentials",
                  BAD_REQUEST),
  USERNAME_NOT_FOUND("401006",
                     "error.username.not.found",
                     BAD_REQUEST),
  INSUFFICIENT_AUTHENTICATION("401007",
                              "error.insufficient.authentication",
                              UNAUTHORIZED),
  LOCKED_EXCEPTION("401008",
                   "error.locked.exception",
                   BAD_REQUEST),
  DISABLED_EXCEPTION("401009",
                     "error.disabled.exception",
                     BAD_REQUEST),

  // conflict
  OPTIMISTIC_LOCKING_ERROR("409001",
                           "error.optimistic.locking",
                           CONFLICT),

  // global error
  GLOBAL_ERROR("500001",
               "error.global",
               HttpStatus.INTERNAL_SERVER_ERROR),
  ENTITY_NOT_FOUND("500002",
                   "error.entity.not.found",
                   NOT_FOUND),

  // custom errors

  // generic errors
  INVALID_REQUEST("400001",
                  "error.invalid.request",
                  BAD_REQUEST),

  // user errors
  USERNAME_ALREADY_EXISTS("400101",
                          "error.username.already.exists",
                          BAD_REQUEST),
  USER_NOT_FOUND("400104",
                 "error.user.not.found",
                 BAD_REQUEST),
  ;
  private final String code;
  private final String message;
  private final HttpStatus httpCode;
}
