package com.gitthub.youssefagagg.ecommerceorderprocessor.repository;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Role} entity.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

  /**
   * Find a role by name.
   *
   * @param name the name to search for
   * @return the role if found, empty otherwise
   */
  Optional<Role> findByName(String name);

  /**
   * Check if a role exists by name.
   *
   * @param name the name to check
   * @return true if the role exists, false otherwise
   */
  boolean existsByName(String name);
}