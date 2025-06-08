package com.gitthub.youssefagagg.ecommerceorderprocessor.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.TracerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * This class implements AuthenticationEntryPoint interface. Then we override the commence method.
 * This method will be triggered anytime unauthenticated User requests a secured endpoint and an
 * AuthenticationException is thrown.
 *
 * @author Youssef Agagg
 */
@Slf4j
@Component
public class JwtAuthorizationEntryPoint extends BaseJwtAuthHandler implements AccessDeniedHandler {

  /**
   * Constructs a JwtAuthorizationEntryPoint instance with the provided ObjectMapper and
   * TracerService.
   *
   * @param objectMapper  an instance of {@code ObjectMapper} used for JSON processing.
   * @param tracerService an instance of {@code TracerService} used to handle tracing and
   *                      context-related information.
   */
  public JwtAuthorizationEntryPoint(ObjectMapper objectMapper, TracerService tracerService) {
    super(objectMapper, tracerService);
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
                     AccessDeniedException accessDeniedException)
      throws IOException, ServletException {
    log.error("Unauthorized error: {}", accessDeniedException.getMessage());
    ErrorCode errorCode = ErrorCode.ACCESS_DENIED;

    generateErrorResponse(response, errorCode);
  }

}
