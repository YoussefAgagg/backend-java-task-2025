package com.gitthub.youssefagagg.ecommerceorderprocessor.user.repository;


import com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity.RefreshToken;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link RefreshToken} entity.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  /**
   * Find a refresh token by token value.
   *
   * @param token the token value to search for
   * @return the refresh token if found, empty otherwise
   */
  Optional<RefreshToken> findByTokenAndRevokedIsFalse(String token);

  /**
   * Find all refresh tokens for a user.
   *
   * @param user the user to search for
   * @return the list of refresh tokens
   */
  List<RefreshToken> findAllByUser(User user);

  /**
   * Revoke all refresh tokens for a user.
   *
   * @param user    the user whose tokens should be revoked
   * @param revoked the revoked flag to set
   * @return the number of tokens updated
   */
  @Modifying
  @Query("UPDATE RefreshToken r SET r.revoked = :revoked WHERE r.user = :user")
  int updateRevokedByUser(@Param("user") User user, @Param("revoked") boolean revoked);

}