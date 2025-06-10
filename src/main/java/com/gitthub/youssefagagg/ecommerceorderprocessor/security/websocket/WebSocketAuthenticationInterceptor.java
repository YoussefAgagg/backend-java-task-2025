package com.gitthub.youssefagagg.ecommerceorderprocessor.security.websocket;

import com.gitthub.youssefagagg.ecommerceorderprocessor.security.jwt.TokenProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * WebSocket handshake interceptor that authenticates users based on JWT tokens. Extracts the token
 * from the handshake request parameters and sets up the SecurityContext.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthenticationInterceptor implements HandshakeInterceptor {

  private final TokenProvider tokenProvider;

  @Override
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) {
    // Extract token from request parameters
    String token = extractTokenFromRequest(request);

    if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
      // Get authentication from token and set it in the SecurityContext
      Authentication authentication = tokenProvider.getAuthentication(token);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      // Store the authentication in the WebSocket session attributes
      // This will make it available during the entire WebSocket session
      attributes.put("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

      log.info("WebSocket connection authenticated for user: {}",
               authentication.getName());
      return true;
    }

    log.warn("No valid JWT token found in WebSocket handshake request");
    return true; // Allow connection even without authentication, security will be enforced later
  }

  @Override
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                             WebSocketHandler wsHandler, Exception exception) {
    // Clean up SecurityContext after handshake to prevent memory leaks
    SecurityContextHolder.clearContext();
  }

  private String extractTokenFromRequest(ServerHttpRequest request) {
    // Extract token from query parameters
    String token = request.getURI().getQuery();
    if (token != null) {
      // Parse query string to find token parameter
      String[] queryParams = token.split("&");
      for (String param : queryParams) {
        if (param.startsWith("token=")) {
          return param.substring(6); // Remove "token=" prefix
        }
      }
    }
    return null;
  }
}