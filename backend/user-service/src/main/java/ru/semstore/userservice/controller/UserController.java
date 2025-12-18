package ru.semstore.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.userservice.dto.user.ChangePasswordDto;
import ru.semstore.userservice.dto.user.UserDto;
import ru.semstore.userservice.exception.ErrorResponse;
import ru.semstore.userservice.security.CustomUserDetails;
import ru.semstore.userservice.service.UserService;

/**
 * REST-контроллер для работы с данными пользователя.
 */
@Tag(name = "Пользователи", description = "API для операций с пользователями")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Validated
public class UserController {
    private final UserService userService;

    /**
     * Возвращает данные текущего аутентифицированного пользователя.
     *
     * @param userDetails данные пользователя из security context
     * @return данные текущего пользователя
     */

    @Operation(summary = "Получение данных текущего пользователя", description = "Возвращает данные пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Данные пользователя возвращены", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "(NOT FOUND) Пользователь не найден", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public UserDto getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.getById(userDetails.user().getId());
    }

    /**
     * Изменяет пароль текущего пользователя.
     *
     * @param userDetails данные текущего пользователя из security context
     * @param dto данные для смены пароля
     */
    @Operation(summary = "Изменение пароля", description = "Позволяет пользователю изменить свой пароль")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Пароль успешно изменен"),
            @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "(NOT FOUND) Пользователь не найден", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409",
                    description = "(CONFLICT) Старый пароль неверен или новый пароль совпадает со старым",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/password")
    public void changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @Valid @RequestBody ChangePasswordDto dto) {
        userService.changePassword(userDetails.user().getId(), dto);
    }
}
