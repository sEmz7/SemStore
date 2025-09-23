package ru.semstore.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.semstore.userservice.dto.user.UserDto;
import ru.semstore.userservice.service.UserService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Validated
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable("userId") UUID userId) {
        return userService.getById(userId);
    }
}
