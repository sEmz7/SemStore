package ru.semstore.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.userservice.dto.jwt.JwtAuthDto;
import ru.semstore.userservice.dto.jwt.RefreshTokenDto;
import ru.semstore.userservice.dto.user.UserCreateDto;
import ru.semstore.userservice.dto.user.UserCredentialsDto;
import ru.semstore.userservice.dto.user.UserDto;
import ru.semstore.userservice.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserCreateDto dto) {
        return userService.create(dto);
    }

    @PostMapping("/login")
    public JwtAuthDto login(@Valid @RequestBody UserCredentialsDto dto) {
        return userService.logIn(dto);
    }

    @PostMapping("/refresh")
    public JwtAuthDto refresh(@Valid @RequestBody RefreshTokenDto dto) {
        return userService.refreshToken(dto);
    }
}
