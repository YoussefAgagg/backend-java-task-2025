package com.gitthub.youssefagagg.ecommerceorderprocessor.common.security;

import lombok.Getter;

/**
 * Enum representation of AuthoritiesConstants.
 *
 * @author Youssef Agagg
 */
@Getter
public enum AuthoritiesRole {
  ROLE_ADMIN("ROLE_ADMIN"),
  ROLE_USER("ROLE_USER"),
  ;

  private final String value;

  AuthoritiesRole(String value) {
    this.value = value;
  }

}
