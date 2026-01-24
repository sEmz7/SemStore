package ru.semstore.userservice.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.userservice.dto.page.PageResponse;
import ru.semstore.userservice.dto.user.UserDtoWithRole;
import ru.semstore.userservice.dto.user.UserRoleUpdateDto;
import ru.semstore.userservice.exception.ErrorResponse;
import ru.semstore.userservice.service.UserService;

import java.util.UUID;

/**
 * REST-контроллер для административного управления пользователями.
 *
 * <p>Контроллер предоставляет API для:
 * <ul>
 *     <li>Получения пользователя по идентификатору</li>
 *     <li>Просмотра списка всех пользователей с пагинацией</li>
 *     <li>Обновления роли пользователя</li>
 * </ul>
 *
 * <p>Все эндпоинты доступны только пользователям с ролью ADMIN
 * и защищены JWT-аутентификацией.</p>
 */
@Tag(name = "admin: Пользователи", description = "API для операций с пользователями для админа")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminUserController {
    private final UserService userService;

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return пользователь с информацией о роли
     */
    @Operation(summary = "Получить пользователя по ID", description = "Возвращает пользователя по указанному ID",
            responses = {@ApiResponse(responseCode = "200", description = "(OK) Пользователь возвращен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDtoWithRole.class))),
                    @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "(NOT FOUND) Пользователь не найден",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping("/{userId}")
    public UserDtoWithRole getUserById(@PathVariable("userId") UUID userId) {
        return userService.getUserByIdWithRole(userId);
    }

    /**
     * Возвращает постраничный список всех пользователей.
     *
     * <p>Результат включает информацию о роли каждого пользователя.</p>
     *
     * @param page номер страницы (начиная с 0)
     * @param size размер страницы
     * @return страница пользователей
     */
    @Operation(summary = "Получить всех пользователей", description = "Возвращает всех пользователей с пагинацией",
            responses = {
                    @ApiResponse(responseCode = "200", description = "(OK) Пользователи возвращены",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = UserDtoWithRole.class)))),
                    @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping
    public PageResponse<UserDtoWithRole> getAllUsersWithRole(
            @PositiveOrZero @RequestParam(name = "page", defaultValue = "0") int page,
            @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        return userService.getAllUsersWithRole(page, size);
    }

    /**
     * Обновляет роль пользователя.
     *
     * <p>Метод используется для административного управления
     * правами доступа пользователей.</p>
     *
     * @param userId идентификатор пользователя
     * @param dto    DTO с новой ролью пользователя
     * @return пользователь с обновлённой ролью
     */
    @Operation(summary = "Обновить роль пользователя по ID",
            description = "Обновляет роль пользователя по ID и возвращает его",
            responses = {
                    @ApiResponse(responseCode = "200", description = "(OK) Пользователь обновлен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserDtoWithRole.class))),
                    @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Невалидные данные запроса",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Невалидный JWT токен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "403", description = "(FORBIDDEN) Доступ запрещен",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "(NOT FOUND) Пользователь не найден",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PatchMapping("/{userId}")
    public UserDtoWithRole updateUserRole(@PathVariable("userId") UUID userId,
                                          @Valid @RequestBody UserRoleUpdateDto dto) {
        return userService.updateUserRole(userId, dto);
    }
}
