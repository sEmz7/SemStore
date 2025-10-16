package ru.semstore.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.semstore.userservice.dto.user.ChangePasswordDto;
import ru.semstore.userservice.dto.user.UserDto;
import ru.semstore.userservice.model.User;
import ru.semstore.userservice.repository.UserRepository;
import ru.semstore.userservice.security.jwt.JwtService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql"})
    @DisplayName("Успешное получение данных пользователя")
    void positiveGetCurrentUserInfo() throws Exception {
        User user = userRepository.findByEmail("test@example.com").orElseThrow();

        String jwt = jwtService.generateAuthToken(user).getToken();

        String result = mvc.perform(get("/users")
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto userDto = objectMapper.readValue(result, UserDto.class);

        assertEquals(user.getEmail(), userDto.email(), "Email должен быть одинаковый");
        assertEquals(user.getId(), userDto.id(), "ID должен быть одинаковый");
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql"})
    @DisplayName("Успешное обновление пароля")
    void positivePasswordUpdate() throws Exception {
        User user = userRepository.findByEmail("test@example.com").orElseThrow();

        String jwt = jwtService.generateAuthToken(user).getToken();

        ChangePasswordDto dto = new ChangePasswordDto("password", "newPassword123");
        String json = objectMapper.writeValueAsString(dto);

        mvc.perform(patch("/users/password")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();

        assertTrue(passwordEncoder.matches(dto.newPassword(), updatedUser.getPassword()));
    }
}