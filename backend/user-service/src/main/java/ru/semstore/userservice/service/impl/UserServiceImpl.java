package ru.semstore.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.userservice.dto.jwt.JwtAuthDto;
import ru.semstore.userservice.dto.user.*;
import ru.semstore.userservice.exception.AuthException;
import ru.semstore.userservice.exception.ConflictException;
import ru.semstore.userservice.exception.ErrorCode;
import ru.semstore.userservice.exception.NotFoundException;
import ru.semstore.userservice.mapper.UserMapper;
import ru.semstore.userservice.model.User;
import ru.semstore.userservice.model.UserRole;
import ru.semstore.userservice.repository.UserRepository;
import ru.semstore.userservice.security.jwt.JwtService;
import ru.semstore.userservice.service.UserService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса пользователей.
 *
 * <p>Отвечает за регистрацию, аутентификацию,
 * работу с JWT токенами и изменение данных пользователя.</p>
 */
@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Регистрирует нового пользователя.
     *
     * @param dto данные для создания пользователя
     * @return созданный пользователь
     * @throws ConflictException если пользователь с таким email уже существует
     */
    @Override
    public UserDto create(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Email={} already exists", dto.email());
            throw new ConflictException("User with email=" + dto.email() + " already exists.",
                    ErrorCode.USER_ALREADY_EXISTS);
        }
        User user = userMapper.toEntity(dto, passwordEncoder);
        user.setRole(UserRole.ROLE_USER);
        user = userRepository.save(user);
        log.debug("Saved user={}", user);
        return userMapper.toDto(user);
    }

    /**
     * Аутентифицирует пользователя по email и паролю.
     *
     * @param dto учетные данные пользователя
     * @return JWT токены
     * @throws NotFoundException если пользователь не найден
     * @throws AuthException если пароль неверный
     */
    @Transactional(readOnly = true)
    @Override
    public JwtAuthDto logIn(UserCredentialsDto dto) {
        Optional<User> optionalUser = userRepository.findByEmail(dto.email());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(dto.password(), user.getPassword())) {
                return jwtService.generateAuthToken(user);
            }
            throw new AuthException("Invalid password", ErrorCode.INVALID_PASSWORD);
        }
        throw new NotFoundException("User with email=" + dto.email() + " not found", ErrorCode.USER_NOT_FOUND);
    }

    /**
     * Обновляет JWT токены по refresh token.
     *
     * @param refreshToken refresh token
     * @return новые JWT токены
     * @throws AuthException если refresh token невалиден
     * @throws NotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    @Override
    public JwtAuthDto refreshToken(String refreshToken) {
        if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {
            User user = userRepository.findByEmail(jwtService.getEmailFromToken(refreshToken))
                    .orElseThrow(() -> new NotFoundException("User with not found", ErrorCode.USER_NOT_FOUND));
            return jwtService.refreshBaseToken(user, refreshToken);
        }
        throw new AuthException("Invalid refresh token", ErrorCode.INVALID_REFRESH_TOKEN);
    }

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return пользователь
     * @throws NotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    @Override
    public UserDto getById(UUID userId) {
        return userMapper.toDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found", ErrorCode.USER_NOT_FOUND)));
    }

    /**
     * Изменяет пароль пользователя.
     *
     * @param userId идентификатор пользователя
     * @param dto данные для смены пароля
     * @throws NotFoundException если пользователь не найден
     * @throws ConflictException если старый пароль неверен
     *                          или новый пароль совпадает со старым
     */
    @Override
    public void changePassword(UUID userId, ChangePasswordDto dto) {
        User user = findUserByIdOrThrow(userId);
        if (!passwordEncoder.matches(dto.oldPassword(), user.getPassword())) {
            throw new ConflictException("Old password is not correct", ErrorCode.INVALID_PASSWORD);
        }
        if (passwordEncoder.matches(dto.newPassword(), user.getPassword())) {
            throw new ConflictException("New password must be different from the old one", ErrorCode.SAME_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        log.debug("User with id={} changed password", userId);
    }

    /**
     * Проверяет валидность JWT токена и возвращает пользователя.
     *
     * @param authHeader HTTP заголовок Authorization
     * @return пользователь
     * @throws AuthException если токен невалиден
     * @throws NotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    @Override
    public UserDto validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthException("Invalid token", ErrorCode.INVALID_TOKEN);
        }
        String token = authHeader.substring(7).trim();
        boolean validated = jwtService.validateJwtToken(token);
        if (!validated) {
            throw new AuthException("Invalid token", ErrorCode.INVALID_TOKEN);
        }
        String email = jwtService.getEmailFromToken(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("User not found by email={}", email);
            return new NotFoundException("User not found. userEmail" + email, ErrorCode.USER_NOT_FOUND);
        });
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDtoWithRole getUserByIdWithRole(UUID userId) {
        User user = findUserByIdOrThrow(userId);
        return userMapper.toDtoWithRole(user);
    }

    private User findUserByIdOrThrow(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id={} not found", userId);
            return new NotFoundException("User not found", ErrorCode.USER_NOT_FOUND);
        });
    }
}
