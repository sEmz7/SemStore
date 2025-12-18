package ru.semstore.userservice.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "DTO для создания адреса")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressCreateDto {

    @Schema(description = "Имя", example = "Иван", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 100)
    private String firstname;

    @Schema(description = "Фамилия", example = "Иванов", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 100)
    private String lastname;

    @Schema(description = "Отчество", example = "Иванович", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 100)
    private String patronymic;

    @Schema(description = "Телефон", example = "+7 (999) 123-45-67", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 50)
    private String phone;

    @Schema(description = "Город", example = "Москва", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 100)
    private String city;

    @Schema(description = "Улица", example = "Тверская", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 100)
    private String street;

    @Schema(description = "Строение", example = "5к1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 100)
    private String building;

    @Schema(description = "Почтовый индекс", example = "125009", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String postalCode;
}
