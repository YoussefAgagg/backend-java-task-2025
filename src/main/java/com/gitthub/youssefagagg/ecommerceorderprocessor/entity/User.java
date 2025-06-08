package com.gitthub.youssefagagg.ecommerceorderprocessor.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * User entity for storing user information.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "roles"})
public class User extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Size(min = 3,
        max = 50)
  @Column(name = "username",
          length = 50,
          unique = true,
          nullable = false)
  private String username;

  @NotNull
  @Size(min = 60,
        max = 60)
  @Column(name = "password_hash",
          length = 60,
          nullable = false)
  private String password;

  @NotNull
  @Size(max = 50)
  @Column(name = "first_name",
          length = 50)
  private String firstName;

  @NotNull
  @Size(max = 50)
  @Column(name = "last_name",
          length = 50)
  private String lastName;

  @Email
  @Size(min = 5,
        max = 255)
  @Column(name = "email",
          length = 255)
  private String email;

  @Pattern(regexp = "^\\+?[0-9]{10,15}$")
  @Column(name = "phone",
          length = 20)
  private String phone;

  @ManyToMany
  @JoinTable(
      name = "user_role",
      joinColumns = @JoinColumn(name = "user_id",
                                referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "role_name",
                                       referencedColumnName = "name")
  )
  @Builder.Default
  private Set<Role> roles = new HashSet<>();
}
