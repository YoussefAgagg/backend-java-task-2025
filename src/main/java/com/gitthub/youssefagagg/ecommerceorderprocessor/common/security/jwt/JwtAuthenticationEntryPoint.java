package com.gitthub.youssefagagg.ecommerceorderprocessor.common.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.service.TracerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
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
public class JwtAuthenticationEntryPoint extends BaseJwtAuthHandler
    implements AuthenticationEntryPoint {


  /**
   * Constructs an instance of JwtAuthenticationEntryPoint.
   *
   * @param objectMapper  An instance of {@code ObjectMapper} used for JSON processing.
   * @param tracerService An instance of {@code TracerService} responsible for retrieving trace
   *                      information.
   */
  public JwtAuthenticationEntryPoint(ObjectMapper objectMapper, TracerService tracerService) {
    super(objectMapper, tracerService);
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    log.error("Unauthorized error: {}", authException.getMessage());
    ErrorCode errorCode = ErrorCode.INSUFFICIENT_AUTHENTICATION;

    generateErrorResponse(response, errorCode);


  }
}
