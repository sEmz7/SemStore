package ru.semstore.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.semstore.userservice.dto.address.AddressCreateDto;
import ru.semstore.userservice.dto.address.AddressDto;
import ru.semstore.userservice.security.CustomUserDetails;
import ru.semstore.userservice.service.AddressService;

@RestController
@RequestMapping("/users/address")
@Validated
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @PostMapping
    public AddressDto createAddress(@Valid @RequestBody AddressCreateDto dto,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        return addressService.create(dto, userDetails.user().getId());
    }
}
