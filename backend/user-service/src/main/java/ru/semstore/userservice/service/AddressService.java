package ru.semstore.userservice.service;

import ru.semstore.userservice.dto.address.AddressCreateDto;
import ru.semstore.userservice.dto.address.AddressDto;

import java.util.UUID;

public interface AddressService {

    AddressDto create(AddressCreateDto dto, UUID userId);
}
