package ru.semstore.userservice.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressCreateDto {

    @NotBlank
    @Size(min = 1, max = 100)
    private String firstname;

    @NotBlank
    @Size(min = 1, max = 100)
    private String lastname;

    @NotBlank
    @Size(min = 1, max = 100)
    private String patronymic;

    @NotBlank
    @Size(min = 1, max = 50)
    private String phone;

    @NotBlank
    @Size(min = 1, max = 100)
    private String city;

    @NotBlank
    @Size(min = 1, max = 100)
    private String street;

    @NotBlank
    @Size(min = 1, max = 100)
    private String building;

    @NotNull
    private Integer postalCode;
}
