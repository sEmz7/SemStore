package ru.semstore.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.semstore.userservice.dto.user.UserDtoWithRole;
import ru.semstore.userservice.service.UserService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public UserDtoWithRole getUserById(@PathVariable("userId") UUID userId) {
        return userService.getUserByIdWithRole(userId);
    }
}
