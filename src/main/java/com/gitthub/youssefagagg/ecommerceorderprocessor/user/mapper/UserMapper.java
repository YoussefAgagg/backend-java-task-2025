package com.gitthub.youssefagagg.ecommerceorderprocessor.user.mapper;


import com.gitthub.youssefagagg.ecommerceorderprocessor.common.mapper.EntityMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.CreateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.UserDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity.Role;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for the entity {@link User} and its DTO {@link UserDTO}.
 */
@Mapper(componentModel = "spring")
public interface UserMapper extends EntityMapper<UserDTO, User> {

  @Mapping(target = "roles",
           source = "roles",
           qualifiedByName = "rolesToStrings")
  UserDTO toDto(User user);

  @Named("rolesToStrings")
  default Set<String> rolesToStrings(Set<Role> roles) {
    return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
  }

  @Mapping(target = "password",
           ignore = true)
  @Mapping(target = "roles",
           ignore = true)
  User toEntity(UserDTO userDTO);


  User createUserRequestToEntity(CreateUserRequest createUserRequest);


}