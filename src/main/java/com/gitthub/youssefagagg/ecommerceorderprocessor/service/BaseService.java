package com.gitthub.youssefagagg.ecommerceorderprocessor.service;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

/**
 * Base service class providing common functionality for all services.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseService {

  protected final UserRepository userRepository;

  /**
   * Get the current authenticated user.
   *
   * @return the current user
   * @throws CustomException if the user is not authenticated
   */
  protected User getCurrentUser() {
    return SecurityUtils.getCurrentUserUserName()
                        .flatMap(userRepository::findByUsernameIgnoreCase)
                        .orElseThrow(() -> new CustomException(
                            ErrorCode.AUTHENTICATION_CREDENTIALS_NOT_FOUND,
                            "User not authenticated"));
  }


  /**
   * Create a PaginationResponse from a Page.
   *
   * @param page the page
   * @param <T>  the type of the page
   * @return the pagination response
   */
  protected <T> PaginationResponse<T> createPaginationResponse(Page<T> page) {
    return PaginationResponse.createPaginationResponse(page);
  }
}