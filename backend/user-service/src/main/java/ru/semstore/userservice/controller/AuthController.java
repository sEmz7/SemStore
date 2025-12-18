package ru.semstore.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.userservice.dto.jwt.JwtAuthDto;
import ru.semstore.userservice.dto.jwt.RefreshTokenDto;
import ru.semstore.userservice.dto.user.UserCreateDto;
import ru.semstore.userservice.dto.user.UserCredentialsDto;
import ru.semstore.userservice.dto.user.UserDto;
import ru.semstore.userservice.exception.ErrorResponse;
import ru.semstore.userservice.service.UserService;

/**
 * REST-контроллер для аутентификации пользователей.
 *
 * <p>Предоставляет API для:
 * <ul>
 *   <li>регистрации новых пользователей</li>
 *   <li>авторизации и получения JWT токенов</li>
 *   <li>обновления access token по refresh token</li>
 *   <li>валидации JWT токена</li>
 * </ul>
 */

@Tag(name = "Аутентификация", description = "API для регистрации, авторизации и обновления токенов пользователей")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final UserService userService;

    /**
     * Регистрирует нового пользователя.
     *
     * @param dto данные для регистрации пользователя
     * @return зарегистрированный пользователь
     */

    @Operation(summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя и возвращает его данные")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "(CREATED) Пользователь успешно зарегистрирован",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Неверные входные данные",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "(CONFLICT) Пользователь с таким email уже существует",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserCreateDto dto) {
        return userService.create(dto);
    }

    /**
     * Аутентифицирует пользователя и возвращает JWT токены.
     *
     * @param dto учетные данные пользователя
     * @return access и refresh JWT токены
     */

    @Operation(summary = "Авторизация", description = "Аутентификация пользователя и получение JWT токенов")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Успешная авторизация", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = JwtAuthDto.class))),
            @ApiResponse(responseCode = "400", description = "(BAD REQUEST) Ошибка входных данных", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный пароль", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "(NOT FOUND) Пользователь не найден", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public JwtAuthDto login(@Valid @RequestBody UserCredentialsDto dto) {
        return userService.logIn(dto);
    }

    /**
     * Обновляет JWT токены с использованием refresh token.
     *
     * @param dto refresh token
     * @return новые access и refresh токены
     */

    @Operation(summary = "Обновление токена доступа",
            description = "Обновление JWT токена доступа с использованием refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Токен успешно обновлен", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = JwtAuthDto.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный или просроченный refresh token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public JwtAuthDto refresh(@Valid @RequestBody RefreshTokenDto dto) {
        return userService.refreshToken(dto);
    }

    /**
     * Проверяет валидность JWT токена и возвращает данные пользователя.
     *
     * @param authHeader HTTP заголовок Authorization с JWT токеном
     * @return данные пользователя, извлечённые из токена
     */

    @PostMapping("/validateToken")
    public UserDto validateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return userService.validateToken(authHeader);
    }
}
