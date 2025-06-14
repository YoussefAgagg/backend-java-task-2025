package com.gitthub.youssefagagg.ecommerceorderprocessor.mapper;


import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.CreateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.UserDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Role;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

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

  @Override
  @Named("partialUpdate")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "roles",
           ignore = true)
  void partialUpdate(@MappingTarget User entity, UserDTO dto);
}