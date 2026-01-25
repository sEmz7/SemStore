package ru.semstore.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.semstore.userservice.dto.address.AddressCreateDto;
import ru.semstore.userservice.dto.address.AddressUpdateDto;
import ru.semstore.userservice.model.Address;
import ru.semstore.userservice.model.User;
import ru.semstore.userservice.repository.AddressRepository;
import ru.semstore.userservice.repository.UserRepository;
import ru.semstore.userservice.security.jwt.JwtService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AddressControllerTests {

    private final static String HEADER_AUTH_NAME = "Authorization";
    private final static String HEADER_BEARER = "Bearer ";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private String jwt;

    @BeforeEach
    void setUp() {
        user = userRepository.findByEmail("test@example.com").orElseThrow();
        jwt = jwtService.generateAuthToken(user).getToken();
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql"})
    @DisplayName("Успешное создание адреса")
    void positiveCreateAddress() throws Exception {
        AddressCreateDto createDto = new AddressCreateDto(
                "Иван", "Иванов", "Иванович", "+7 (999) 123-45-67", "Москва",
                "Тверская", "15", "125009"
        );

        mvc.perform(post("/users/address")
                    .header(HEADER_AUTH_NAME, HEADER_BEARER + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());

        List<Address> addresses = addressRepository.findAllByUserIdAndDeleted(user.getId(), false);

        assertNotNull(addresses);
        assertEquals(user.getId(), addresses.getFirst().getUser().getId());
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql", "/data/address_insert.sql"})
    @DisplayName("Успешное получение всех адресов пользователя")
    void positiveGetUserAddresses() throws Exception {
        mvc.perform(get("/users/address")
                        .header(HEADER_AUTH_NAME, HEADER_BEARER + jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql", "/data/address_insert.sql"})
    @DisplayName("Успешное получение адреса по ID")
    void positiveGetAddressById() throws Exception {
        Address address = addressRepository.findAllByUserIdAndDeleted(user.getId(), false).getFirst();

        mvc.perform(get("/users/address/{id}", address.getId())
                        .header(HEADER_AUTH_NAME, HEADER_BEARER + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(address.getId().toString()));
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql", "/data/address_insert.sql"})
    @DisplayName("Успешное обновление адреса")
    void positiveUpdateAddress() throws Exception {
        Address address = addressRepository.findAllByUserIdAndDeleted(user.getId(), false).getFirst();

        AddressUpdateDto updateDto = new AddressUpdateDto();
        updateDto.setCity("Санкт-Петербург");
        updateDto.setStreet("Невский проспект");

        mvc.perform(patch("/users/address/{id}", address.getId())
                        .header(HEADER_AUTH_NAME, HEADER_BEARER + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Санкт-Петербург"))
                .andExpect(jsonPath("$.street").value("Невский проспект"));

        Address updated = addressRepository.findById(address.getId()).orElseThrow();
        assertEquals("Санкт-Петербург", updated.getCity());
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insert.sql", "/data/address_insert.sql"})
    @DisplayName("Успешное удаление адреса")
    void positiveDeleteAddress() throws Exception {
        Address address = addressRepository.findAllByUserIdAndDeleted(user.getId(), false).getFirst();

        mvc.perform(delete("/users/address/{id}", address.getId())
                        .header(HEADER_AUTH_NAME, HEADER_BEARER + jwt))
                .andExpect(status().isNoContent());

        Optional<Address> optionalAddress = addressRepository.findById(address.getId());

        assertTrue(optionalAddress.isPresent());
        assertTrue(optionalAddress.get().isDeleted());
    }
}
