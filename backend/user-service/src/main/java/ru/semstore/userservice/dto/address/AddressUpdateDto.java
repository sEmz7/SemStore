package ru.semstore.userservice.dto.address;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressUpdateDto {

    @Size(min = 1, max = 100)
    private String firstname;

    @Size(min = 1, max = 100)
    private String lastname;

    @Size(min = 1, max = 100)
    private String patronymic;

    @Size(min = 1, max = 50)
    private String phone;

    @Size(min = 1, max = 100)
    private String city;

    @Size(min = 1, max = 100)
    private String street;

    @Size(min = 1, max = 100)
    private String building;

    private Integer postalCode;
}
