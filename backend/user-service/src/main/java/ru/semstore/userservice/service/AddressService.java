package ru.semstore.userservice.service;

import ru.semstore.userservice.dto.address.AddressCreateDto;
import ru.semstore.userservice.dto.address.AddressDto;
import ru.semstore.userservice.dto.address.AddressUpdateDto;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    AddressDto create(AddressCreateDto dto, UUID userId);

    List<AddressDto> getUserAddresses(UUID userId);

    AddressDto update(AddressUpdateDto dto, UUID addressId);

    AddressDto getById(UUID addressId);

    void delete(UUID addressId);
}
