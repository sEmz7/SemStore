package ru.semstore.userservice.service;

import ru.semstore.userservice.dto.jwt.JwtAuthDto;
import ru.semstore.userservice.dto.jwt.RefreshTokenDto;
import ru.semstore.userservice.dto.user.ChangePasswordDto;
import ru.semstore.userservice.dto.user.UserCredentialsDto;
import ru.semstore.userservice.dto.user.UserDto;
import ru.semstore.userservice.dto.user.UserCreateDto;

import java.util.UUID;

public interface UserService {

    UserDto create(UserCreateDto dto);

    JwtAuthDto logIn(UserCredentialsDto dto);

    JwtAuthDto refreshToken(RefreshTokenDto refreshTokenDto);

    UserDto getById(UUID userId);

    void changePassword(UUID userId, ChangePasswordDto dto);

    UserDto validateToken(String authHeader);
}
