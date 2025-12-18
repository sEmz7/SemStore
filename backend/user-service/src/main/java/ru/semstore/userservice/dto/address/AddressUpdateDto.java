package ru.semstore.userservice.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO для обновления адреса")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressUpdateDto {

    @Schema(description = "Имя", example = "Иван", minLength = 1, maxLength = 100)
    @Size(min = 1, max = 100)
    private String firstname;

    @Schema(description = "Фамилия", example = "Иванов", minLength = 1, maxLength = 100)
    @Size(min = 1, max = 100)
    private String lastname;

    @Schema(description = "Отчество", example = "Иванович", minLength = 1, maxLength = 100)
    @Size(min = 1, max = 100)
    private String patronymic;

    @Schema(description = "Телефон", example = "+7 (999) 123-45-67", minLength = 1, maxLength = 50)
    @Size(min = 1, max = 50)
    private String phone;

    @Schema(description = "Город", example = "Москва", minLength = 1, maxLength = 100)
    @Size(min = 1, max = 100)
    private String city;

    @Schema(description = "Улица", example = "Тверская", minLength = 1, maxLength = 100)
    @Size(min = 1, max = 100)
    private String street;

    @Schema(description = "Строение", example = "5к1", minLength = 1, maxLength = 100)
    @Size(min = 1, max = 100)
    private String building;

    @Schema(description = "Почтовый индекс", example = "125009")
    private String postalCode;
}
