package com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity;

import com.gitthub.youssefagagg.ecommerceorderprocessor.common.entity.AbstractAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RefreshToken entity for storing refresh token information.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(name = "token",
          nullable = false,
          unique = true)
  private String token;

  @ManyToOne
  @JoinColumn(name = "user_id",
              referencedColumnName = "id")
  private User user;

  @NotNull
  @Column(name = "expiry_date",
          nullable = false)
  private Instant expiryDate;

  @Column(name = "issued_date")
  private Instant issuedDate;

  @NotNull
  @Column(name = "revoked",
          nullable = false)
  @Builder.Default
  private boolean revoked = false;

  @Column(name = "device_info",
          length = 255)
  private String deviceInfo;

  @Column(name = "user_agent",
          length = 500)
  private String userAgent;

  @Column(name = "ip_address",
          length = 45)
  private String ipAddress;

  @Column(name = "last_login_date")
  private Instant lastLoginDate;

  /**
   * Check if the token is expired. Note: As per requirement, refresh tokens do not expire.
   *
   * @return always false as tokens don't expire
   */
  public boolean isExpired() {
    // Tokens don't expire as per requirement
    return false;
  }

  /**
   * Check if the token is valid (not revoked). Note: As per requirement, we only check if the token
   * is revoked, not if it's expired.
   *
   * @return true if the token is valid, false otherwise
   */
  public boolean isValid() {
    return !revoked;
  }
}
