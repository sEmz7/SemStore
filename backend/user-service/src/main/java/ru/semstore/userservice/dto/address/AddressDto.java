package ru.semstore.userservice.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.semstore.userservice.dto.user.UserDto;

import java.util.UUID;

@Schema(description = "DTO адреса")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {
    @Schema(description = "ID адреса", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "ID пользователя", example = "e9312918-a9b3-4eaa-9bb1-f97b88b58572")
    private UserDto user;

    @Schema(description = "Имя", example = "Иван")
    private String firstname;

    @Schema(description = "Фамилия", example = "Иванов")
    private String lastname;

    @Schema(description = "Отчество", example = "Иванович")
    private String patronymic;

    @Schema(description = "Телефон", example = "+7 (999) 123-45-67")
    private String phone;

    @Schema(description = "Город", example = "Москва")
    private String city;

    @Schema(description = "Улица", example = "Тверская")
    private String street;

    @Schema(description = "Строение", example = "5к1")
    private String building;

    @Schema(description = "Почтовый индекс", example = "125009")
    private Integer postalCode;
}
