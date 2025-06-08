package com.gitthub.youssefagagg.ecommerceorderprocessor.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.response.ApiErrorResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.TracerService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
@RequiredArgsConstructor
public abstract class BaseJwtAuthHandler {

  private final ObjectMapper objectMapper;
  private final TracerService tracerService;

  /**
   * Generates an error response to be sent back to the client.
   *
   * @param response  the HttpServletResponse object used to send the error response
   * @param errorCode the error code that contains details about the error
   * @throws IOException if an I/O error occurs during response writing
   */
  public void generateErrorResponse(HttpServletResponse response,
                                    ErrorCode errorCode)
      throws IOException {
    ApiErrorResponse errorResponse = new ApiErrorResponse(errorCode.getHttpCode(),
                                                          errorCode.getCode(),
                                                          errorCode.name(),
                                                          errorCode.getMessage());
    errorResponse.setTraceParent(tracerService.getTraceParent());
    response.setStatus(errorResponse.getHttpStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
