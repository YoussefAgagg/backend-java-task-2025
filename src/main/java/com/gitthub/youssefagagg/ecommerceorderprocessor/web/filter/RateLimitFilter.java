package com.gitthub.youssefagagg.ecommerceorderprocessor.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.RateLimitService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter to apply rate limiting to API requests.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimitService rateLimitService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain)
      throws ServletException, IOException {

    // Only apply rate limiting to API requests
    String path = request.getRequestURI();
    if (!path.startsWith("/api")) {
      filterChain.doFilter(request, response);
      return;
    }

    // Get client IP address
    String clientIp = getClientIP(request);
    String method = request.getMethod();

    log.debug("Rate limiting request: {} {} from IP: {}", method, path, clientIp);

    // Get the bucket for this client and path
    Bucket bucket = rateLimitService.resolveBucket(clientIp, path);

    // Try to consume a token
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    // If successful, add rate limit headers and proceed
    if (probe.isConsumed()) {
      long remainingTokens = probe.getRemainingTokens();
      long secondsToWait = probe.getNanosToWaitForRefill() / 1_000_000_000;

      response.addHeader("X-Rate-Limit-Remaining", String.valueOf(remainingTokens));
      response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(secondsToWait));

      log.debug("Request allowed: {} {} from IP: {}. Remaining tokens: {}",
                method, path, clientIp, remainingTokens);

      filterChain.doFilter(request, response);
    } else {
      // If rate limit exceeded, return 429 Too Many Requests
      long secondsToWait = probe.getNanosToWaitForRefill() / 1_000_000_000;

      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);

      Map<String, Object> errorDetails = new HashMap<>();
      errorDetails.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
      errorDetails.put("error", "Too Many Requests");
      errorDetails.put("message",
                       "Rate limit exceeded. Try again in " + secondsToWait + " seconds");

      response.getWriter().write(objectMapper.writeValueAsString(errorDetails));

      log.warn("Rate limit exceeded for request: {} {} from IP: {}. Retry after: {} seconds",
               method, path, clientIp, secondsToWait);
    }
  }

  /**
   * Extract the client IP address from the request. Handles cases where the request comes through a
   * proxy.
   *
   * @param request the HTTP request
   * @return the client IP address
   */
  private String getClientIP(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      // X-Forwarded-For can contain multiple IPs, the first one is the client
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
