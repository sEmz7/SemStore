package ru.semstore.userservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.semstore.userservice.dto.jwt.JwtAuthDto;
import ru.semstore.userservice.dto.jwt.RefreshTokenDto;
import ru.semstore.userservice.dto.user.UserCreateDto;
import ru.semstore.userservice.dto.user.UserCredentialsDto;
import ru.semstore.userservice.model.User;
import ru.semstore.userservice.repository.UserRepository;
import ru.semstore.userservice.security.jwt.JwtService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Sql(scripts = "/data/cleanUp.sql")
    @DisplayName("Удачная регистрация пользователя")
    void positiveRegistrationTest() throws Exception {
        UserCreateDto createDto = new UserCreateDto("mail@mail.com", "pass");

        String userJson = objectMapper.writeValueAsString(createDto);

        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

        List<User> users = userRepository.findAll();
        assertNotNull(users);

        assertEquals(1, users.size());
        assertEquals(createDto.email(), users.getFirst().getEmail());
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql"})
    @DisplayName("Удачная авторизация пользователя")
    void positiveLoginTest() throws Exception {
        UserCredentialsDto credentialsDto = new UserCredentialsDto("test@example.com", "password");

        String credentialsJson = objectMapper.writeValueAsString(credentialsDto);

        String result = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentialsJson))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

        JwtAuthDto jwtDto = objectMapper.readValue(result, JwtAuthDto.class);

        assertEquals(credentialsDto.email(), jwtService.getEmailFromToken(jwtDto.getToken()));
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql"})
    @DisplayName("Неудачная авторизация: неверный пароль")
    void negativeLoginTest() throws Exception {
        UserCredentialsDto credentialsDto = new UserCredentialsDto("test@example.com", "bad_pass");

        String credentialsJson = objectMapper.writeValueAsString(credentialsDto);

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql"})
    @DisplayName("Удачное обновление refresh токена")
    void refreshTokenTest() throws Exception {
        UserCredentialsDto credentialsDto = new UserCredentialsDto("test@example.com", "password");

        String credentialsJson = objectMapper.writeValueAsString(credentialsDto);

        String result = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtAuthDto jwtDto = objectMapper.readValue(result, JwtAuthDto.class);
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(jwtDto.getRefreshToken());

        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenDto)))
                .andExpect(status().isOk());
    }
}