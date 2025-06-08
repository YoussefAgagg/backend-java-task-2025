package com.gitthub.youssefagagg.ecommerceorderprocessor.security.jwt;

import com.gitthub.youssefagagg.ecommerceorderprocessor.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

/**
 * create and validate jwt token.
 *
 * @author Youssef Agagg
 */

@Slf4j
@Component
public class TokenProvider {


  private static final String AUTHORITIES_KEY = "auth";
  private static final String INVALID_JWT_TOKEN = "Invalid JWT token.";
  private final SecretKey key;
  private final JwtParser jwtParser;
  private final JwtProperties jwtProperties;

  /**
   * Constructs a TokenProvider object using the provided JWT properties. This initializes the JWT
   * parser and signing key required for token validation and creation.
   *
   * @param jwtProperties the properties for JWT configuration
   */
  public TokenProvider(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    byte[] secretBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
    key = Keys.hmacShaKeyFor(secretBytes);
    jwtParser = Jwts.parser().verifyWith(key).build();
  }

  /**
   * Creates a JWT token for the provided {@link Authentication} instance. The token includes user
   * details such as authorities, subject, and expiration time.
   *
   * @param authentication the Authentication object containing user details and authorities
   *                       required to build the token.
   * @return a signed JWT token as a String.
   */
  public String createToken(Authentication authentication) {
    String authorities = authentication.getAuthorities()
                                       .stream()
                                       .map(GrantedAuthority::getAuthority)
                                       .collect(Collectors.joining(","));
    long now = new Date().getTime();
    long tokenExpiredAfter;
    tokenExpiredAfter =
        jwtProperties.getExpiration() * 1000; // Convert seconds to milliseconds

    Date expirationDate = new Date(now + tokenExpiredAfter);
    return Jwts
        .builder()
        .subject(authentication.getName())
        .claim(AUTHORITIES_KEY, authorities)
        .issuer(jwtProperties.getIssuer())
        .signWith(key)
        .expiration(expirationDate)
        .compact();
  }

  /**
   * Retrieves an {@link Authentication} object based on the provided JWT token. This method parses
   * the JWT token to extract claims and authorities, creating an authenticated {@link User}
   * principal.
   *
   * @param token the JWT token from which authentication information is extracted
   * @return an {@link Authentication} object containing principal, token, and authorities
   */
  public Authentication getAuthentication(String token) {
    Claims claims = getAllClaimsFromToken(token);
    var authorities = Arrays
        .stream(claims.get(AUTHORITIES_KEY).toString().split(","))
        .filter(auth -> !auth.isBlank())
        .map(SimpleGrantedAuthority::new)
        .toList();
    log.info("********subject*********");
    log.info(claims.getSubject());
    log.info("********claims*********");
    log.info(String.valueOf(claims));

    User principal = new User(claims.getSubject(), "", authorities);

    return new UsernamePasswordAuthenticationToken(principal, token, authorities);

  }

  /**
   * Validates the provided JWT token.
   *
   * @param authToken the JWT token to validate
   * @return {@code true} if the token is valid, {@code false} otherwise
   */
  public boolean validateToken(String authToken) {
    try {
      jwtParser.parseSignedClaims(authToken);
      log.info("********jwtParser*********");
      return true;
    } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException
             | SignatureException | IllegalArgumentException e) {
      log.debug(INVALID_JWT_TOKEN, e);
      log.trace(INVALID_JWT_TOKEN, e);

    }

    return false;
  }

  /**
   * Extracts all claims from the given JWT token.
   *
   * @param token the JWT token from which claims are to be extracted
   * @return the claims extracted from the provided token
   */
  public Claims getAllClaimsFromToken(String token) {
    return jwtParser.parseSignedClaims(token).getPayload();
  }
}
