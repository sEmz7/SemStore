package ru.semstore.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.semstore.userservice.dto.address.AddressCreateDto;
import ru.semstore.userservice.dto.address.AddressDto;
import ru.semstore.userservice.dto.address.AddressUpdateDto;
import ru.semstore.userservice.security.CustomUserDetails;
import ru.semstore.userservice.service.AddressService;

import java.util.List;
import java.util.UUID;

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

    @GetMapping
    public List<AddressDto> getUserAddresses(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return addressService.getUserAddresses(userDetails.user().getId());
    }

    @PatchMapping("/{addressId}")
    public AddressDto updateAddress(@Valid @RequestBody AddressUpdateDto dto,
                                    @PathVariable("addressId") UUID addressId) {
        return addressService.update(dto, addressId);
    }

    @GetMapping("/{addressId}")
    public AddressDto getAddressById(@PathVariable("addressId") UUID addressId) {
        return addressService.getById(addressId);
    }

    @DeleteMapping("/{addressId}")
    public void deleteAddressById(@PathVariable("addressId") UUID addressId) {
        addressService.delete(addressId);
    }
}
