package ru.semstore.userservice.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.userservice.dto.jwt.AccessTokenDto;
import ru.semstore.userservice.dto.jwt.JwtAuthDto;
import ru.semstore.userservice.dto.user.UserCreateDto;
import ru.semstore.userservice.dto.user.UserCredentialsDto;
import ru.semstore.userservice.dto.user.UserDto;
import ru.semstore.userservice.dto.user.UserDtoWithRole;
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
     * Аутентифицирует пользователя, устанавливает refresh токен в cookie и возвращает access токен.
     *
     * @param dto учетные данные пользователя
     * @return access токен
     */
    @Operation(summary = "Авторизация", description = "Аутентификация пользователя и получение JWT токенов")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Успешная авторизация", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AccessTokenDto.class))),
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
    public ResponseEntity<AccessTokenDto> login(@Valid @RequestBody UserCredentialsDto dto) {
        JwtAuthDto jwt = userService.logIn(dto);
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", jwt.getRefreshToken())
                .httpOnly(true)
                .path("/")
                .secure(false)
                .sameSite("Lax")
                .maxAge(30L * 24 * 60 * 60)
                .build();
        AccessTokenDto accessTokenDto = new AccessTokenDto(jwt.getToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(accessTokenDto);
    }

    /**
     * Обновляет access токен с использованием refresh токена.
     *
     * @param refreshToken refresh токен из cookie
     * @return новый access токен
     */
    @Operation(summary = "Обновление токена доступа",
            description = "Обновление JWT токена доступа с использованием refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "(OK) Токен успешно обновлен", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AccessTokenDto.class))),
            @ApiResponse(responseCode = "401", description = "(UNAUTHORIZED) Неверный или просроченный refresh token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenDto> refresh(@CookieValue(name = "refreshToken", required = false)
                                                      String refreshToken) {
        JwtAuthDto jwt = userService.refreshToken(refreshToken);
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", jwt.getRefreshToken())
                .httpOnly(true)
                .path("/")
                .secure(false)
                .maxAge(30L * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
        AccessTokenDto accessTokenDto = new AccessTokenDto(jwt.getToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(accessTokenDto);
    }

    /**
     * Проверяет валидность JWT токена и возвращает данные пользователя.
     *
     * @param authHeader HTTP заголовок Authorization с JWT токеном
     * @return данные пользователя, извлечённые из токена
     */
    @PostMapping("/validateToken")
    public UserDtoWithRole validateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return userService.validateToken(authHeader);
    }

    /**
     * Выход из системы. Удаляет refresh токен из cookie.
     */
    @Operation(summary = "Выход из системы", description = "Удаляет refresh токен из cookie",
    responses = {
            @ApiResponse(responseCode = "204", description = "(NO CONTENT) Токен успешно удален")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .secure(false)
                .sameSite("Lax")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }
}
