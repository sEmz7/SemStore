package ru.semstore.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.userservice.dto.address.AddressCreateDto;
import ru.semstore.userservice.dto.address.AddressDto;
import ru.semstore.userservice.dto.address.AddressUpdateDto;
import ru.semstore.userservice.exception.ErrorResponse;
import ru.semstore.userservice.security.CustomUserDetails;
import ru.semstore.userservice.service.AddressService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Адреса", description = "API для операций с адресами")
@RestController
@RequestMapping("/users/address")
@Validated
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @Operation(
            summary = "Создание нового адреса",
            description = "Создает новый адрес и возвращает его данные"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Адрес успешно создан",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AddressDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные данные запроса",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь имеет максимальное количество адресов",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressDto createAddress(@Valid @RequestBody AddressCreateDto dto,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        return addressService.create(dto, userDetails.user().getId());
    }

    @Operation(
            summary = "Получение всех адресов пользователя",
            description = "Возвращает данные обо всех адресах пользователя"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Адреса возвращены",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = AddressDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping
    public List<AddressDto> getUserAddresses(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return addressService.getUserAddresses(userDetails.user().getId());
    }

    @Operation(
            summary = "Обновление адреса",
            description = "Обновляет данные существующего адреса"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Адрес успешно обновлен",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AddressDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные данные запроса",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Адрес не найден",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Попытка обновления чужого адреса",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                            )
            )
    })
    @PatchMapping("/{addressId}")
    public AddressDto updateAddress(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @Valid @RequestBody AddressUpdateDto dto,
                                    @PathVariable("addressId") UUID addressId) {
        return addressService.update(dto, addressId, userDetails.user().getId());
    }

    @Operation(
            summary = "Получение адреса по ID",
            description = "Возвращает данные конкретного адреса по его идентификатору"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Адрес возвращен",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AddressDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Адрес не найден",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{addressId}")
    public AddressDto getAddressById(@PathVariable("addressId") UUID addressId) {
        return addressService.getById(addressId);
    }

    @Operation(
            summary = "Удаление адреса",
            description = "Удаляет адрес по его идентификатору"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Адрес успешно удален"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Адрес не найден",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Попытка обновления чужого адреса",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddressById(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable("addressId") UUID addressId) {
        addressService.delete(addressId, userDetails.user().getId());
    }
}
