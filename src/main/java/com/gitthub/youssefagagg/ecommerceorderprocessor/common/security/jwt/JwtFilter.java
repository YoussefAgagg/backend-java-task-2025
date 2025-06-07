package com.gitthub.youssefagagg.ecommerceorderprocessor.common.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a
 * valid user is found.
 *
 * @author Youssef Agagg
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  public static final String AUTHORIZATION_HEADER = "Authorization";

  private final TokenProvider tokenProvider;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String jwt = getToken(request);
    if (StringUtils.hasText(jwt)
        && tokenValidation(jwt)) {
      var authentication = tokenProvider.getAuthentication(jwt);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    // log request info
    log.info("Request: {} {}", request.getMethod(), request.getRequestURI());
    // log request headers and values
    Map<String, String> headers = new LinkedHashMap<>();
    request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
      // skip authorization header
      if (!(AUTHORIZATION_HEADER.equalsIgnoreCase(headerName)
            || "Cookie".equalsIgnoreCase(headerName))) {
        headers.put(headerName, request.getHeader(headerName));
      }
    });
    log.info("Request headers: {}", headers);
    filterChain.doFilter(request, response);
  }

  private boolean tokenValidation(String jwtToken) {
    return tokenProvider.validateToken(jwtToken);
  }

  private String getToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
