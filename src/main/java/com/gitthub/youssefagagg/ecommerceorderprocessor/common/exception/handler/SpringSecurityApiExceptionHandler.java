package com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.handler;

import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.response.ApiErrorResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Handles exceptions specific to Spring Security in the API layer. This class implements the
 * {@link ApiExceptionHandler} interface and is responsible for determining whether it can handle a
 * given exception, based on a predefined mapping, and for generating an appropriate
 * {@link ApiErrorResponse}.
 * <br>
 * Exceptions such as {@link AccessDeniedException}, {@link BadCredentialsException}, or
 * {@link UsernameNotFoundException}, among others, are mapped to specific {@link ErrorCode} values
 * which are used to construct the API error response.
 * <br>
 * The exception mapping to error codes is statically defined in this class, allowing for
 * centralized and consistent handling of Spring Security-related exceptions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpringSecurityApiExceptionHandler implements ApiExceptionHandler {

  private static final Map<Class<? extends Exception>, ErrorCode> EXCEPTION_TO_ERROR_CODE_MAPPING;

  static {
    EXCEPTION_TO_ERROR_CODE_MAPPING = new HashMap<>();
    EXCEPTION_TO_ERROR_CODE_MAPPING.put(AccessDeniedException.class, ErrorCode.ACCESS_DENIED);
    EXCEPTION_TO_ERROR_CODE_MAPPING.put(AccountExpiredException.class, ErrorCode.ACCOUNT_EXPIRED);
    EXCEPTION_TO_ERROR_CODE_MAPPING.put(AuthenticationCredentialsNotFoundException.class,
                                        ErrorCode.AUTHENTICATION_CREDENTIALS_NOT_FOUND);
    EXCEPTION_TO_ERROR_CODE_MAPPING.put(AuthenticationServiceException.class,
                                        ErrorCode.AUTHENTICATION_SERVICE_EXCEPTION);
    EXCEPTION_TO_ERROR_CODE_MAPPING.put(BadCredentialsException.class, ErrorCode.BAD_CREDENTIALS);
    EXCEPTION_TO_ERROR_CODE_MAPPING.put(UsernameNotFoundException.class,
                                        ErrorCode.USERNAME_NOT_FOUND);
    EXCEPTION_TO_ERROR_CODE_MAPPING.put(InsufficientAuthenticationException.class,
                                        ErrorCode.INSUFFICIENT_AUTHENTICATION);
    EXCEPTION_TO_ERROR_CODE_MAPPING.put(LockedException.class, ErrorCode.LOCKED_EXCEPTION);
    EXCEPTION_TO_ERROR_CODE_MAPPING.put(DisabledException.class, ErrorCode.DISABLED_EXCEPTION);
  }


  @Override
  public boolean canHandle(Throwable exception) {
    return EXCEPTION_TO_ERROR_CODE_MAPPING.containsKey(exception.getClass());
  }

  @Override
  public ApiErrorResponse handle(Throwable exception) {
    var errorCode =
        EXCEPTION_TO_ERROR_CODE_MAPPING.getOrDefault(exception.getClass(), ErrorCode.GLOBAL_ERROR);

    return new ApiErrorResponse(errorCode.getHttpCode(),
                                errorCode.getCode(),
                                errorCode.name(),
                                errorCode.getMessage());
  }
}
