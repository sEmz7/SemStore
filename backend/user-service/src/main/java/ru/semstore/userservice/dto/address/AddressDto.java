package ru.semstore.userservice.dto.address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.semstore.userservice.dto.user.UserDto;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {
    private UUID id;

    private UserDto user;

    private String firstname;

    private String lastname;

    private String patronymic;

    private String phone;

    private String city;

    private String street;

    private String building;

    private Integer postalCode;
}
