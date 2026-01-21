package ru.semstore.userservice.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.userservice.dto.page.PageResponse;
import ru.semstore.userservice.dto.user.UserDtoWithRole;
import ru.semstore.userservice.service.UserService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminUserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public UserDtoWithRole getUserById(@PathVariable("userId") UUID userId) {
        return userService.getUserByIdWithRole(userId);
    }

    @GetMapping
    public PageResponse<UserDtoWithRole> getAllUsersWithRole(
            @PositiveOrZero @RequestParam(name ="page", defaultValue = "0") int page,
            @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        return userService.getAllUsersWithRole(page, size);
    }
}
