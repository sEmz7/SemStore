package ru.semstore.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.userservice.dto.jwt.JwtAuthDto;
import ru.semstore.userservice.dto.jwt.RefreshTokenDto;
import ru.semstore.userservice.dto.user.UserCreateDto;
import ru.semstore.userservice.dto.user.UserCredentialsDto;
import ru.semstore.userservice.dto.user.UserDto;
import ru.semstore.userservice.exception.AuthException;
import ru.semstore.userservice.exception.ConflictException;
import ru.semstore.userservice.exception.NotFoundException;
import ru.semstore.userservice.mapper.UserMapper;
import ru.semstore.userservice.model.User;
import ru.semstore.userservice.repository.UserRepository;
import ru.semstore.userservice.security.jwt.JwtService;
import ru.semstore.userservice.service.UserService;

import java.util.Optional;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserDto create(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Email={} already exists", dto.email());
            throw new ConflictException("User with email=" + dto.email() + " already exists.");
        }
        User user = userMapper.toEntity(dto, passwordEncoder);
        user = userRepository.save(user);
        log.debug("Saved user={}", user);
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public JwtAuthDto logIn(UserCredentialsDto dto) {
        Optional<User> optionalUser = userRepository.findByEmail(dto.email());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(dto.password(), user.getPassword())) {
                return jwtService.generateAuthToken(user.getEmail());
            }
            throw new AuthException("Invalid password");
        }
        throw new NotFoundException("User with email=" + dto.email() + " not found");
    }

    @Transactional(readOnly = true)
    @Override
    public JwtAuthDto refreshToken(RefreshTokenDto refreshTokenDto) {
        String refreshToken = refreshTokenDto.getRefreshToken();
        if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {
            User user = userRepository.findByEmail(jwtService.getEmailFromToken(refreshToken))
                    .orElseThrow(() -> new NotFoundException("User with not found"));
            return jwtService.refreshBaseToken(user.getEmail(), refreshToken);
        }
        throw new AuthException("Invalid refresh token");
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getById(UUID userId) {
        return userMapper.toDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found")));
    }
}
