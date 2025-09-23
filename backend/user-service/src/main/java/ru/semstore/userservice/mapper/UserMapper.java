package ru.semstore.userservice.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.semstore.userservice.dto.user.UserCreateDto;
import ru.semstore.userservice.dto.user.UserDto;
import ru.semstore.userservice.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "password", expression = "java(passwordEncoder.encode(dto.password()))")
    User toEntity(UserCreateDto dto, @Context PasswordEncoder passwordEncoder);

    UserDto toDto(User entity);
}
