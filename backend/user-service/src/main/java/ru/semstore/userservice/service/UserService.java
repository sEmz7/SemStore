package ru.semstore.userservice.service;

import ru.semstore.userservice.dto.auth.JwtAuthDto;
import ru.semstore.userservice.dto.page.PageResponse;
import ru.semstore.userservice.dto.user.*;

import java.util.UUID;

/**
 * Сервис для управления пользователями и аутентификацией.
 */
public interface UserService {

    /**
     * Регистрирует нового пользователя.
     *
     * @param dto данные для регистрации пользователя
     * @return созданный пользователь
     */
    UserDto create(UserCreateDto dto);

    /**
     * Аутентифицирует пользователя.
     *
     * @param dto учетные данные пользователя
     * @return JWT токены
     */
    JwtAuthDto logIn(UserCredentialsDto dto);

    /**
     * Обновляет JWT токен по refresh token.
     *
     * @param refreshToken refresh token
     * @return JWT токены
     */
    JwtAuthDto refreshToken(String refreshToken);

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return пользователь
     */
    UserDto getById(UUID userId);

    /**
     * Изменяет пароль пользователя.
     *
     * @param userId идентификатор пользователя
     * @param dto    данные для смены пароля
     */
    void changePassword(UUID userId, ChangePasswordDto dto);

    /**
     * Проверяет JWT токен и возвращает пользователя.
     *
     * @param authHeader HTTP заголовок Authorization
     * @return пользователь
     */
    UserDtoWithRole validateToken(String authHeader);

    /**
     * Возвращает пользователя по идентификатору с информацией о роли.
     *
     * @param userId идентификатор пользователя
     * @return пользователь с ролью
     */
    UserDtoWithRole getUserByIdWithRole(UUID userId);

    /**
     * Возвращает постраничный список всех пользователей с ролями.
     *
     * @param page номер страницы (начиная с 0)
     * @param size размер страницы
     * @return страница пользователей с ролями
     */
    PageResponse<UserDtoWithRole> getAllUsersWithRole(int page, int size);

    /**
     * Обновляет роль пользователя.
     *
     * @param userId идентификатор пользователя
     * @param dto    DTO с новой ролью
     * @return пользователь с обновлённой ролью
     */
    UserDtoWithRole updateUserRole(UUID userId, UserRoleUpdateDto dto);
}
