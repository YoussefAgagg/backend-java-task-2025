package com.gitthub.youssefagagg.ecommerceorderprocessor.user.repository;

import com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

  /**
   * Find a user by username.
   *
   * @param username the username to search for
   * @return the user if found, empty otherwise
   */
  Optional<User> findByUsernameIgnoreCase(String username);


  /**
   * Check if a username exists.
   *
   * @param username the username to check
   * @return true if the username exists, false otherwise
   */
  boolean existsByUsernameIgnoreCase(String username);


  /**
   * Check if an email exists for a non-deleted user.
   *
   * @param email the email to check
   * @return true if the email exists for a non-deleted user, false otherwise
   */
  boolean existsByEmailIgnoreCaseAndDeletedFalse(String email);


  /**
   * Retrieves a non-deleted user entity by their username, ignoring case sensitivity.
   *
   * @param username the username of the user to search for
   * @return an optional containing the user if found and not deleted, or an empty optional
   *     otherwise
   */
  Optional<User> findByUsernameIgnoreCaseAndDeletedIsFalse(String username);
}
