package ru.semstore.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.userservice.dto.address.AddressCreateDto;
import ru.semstore.userservice.dto.address.AddressDto;
import ru.semstore.userservice.dto.address.AddressUpdateDto;
import ru.semstore.userservice.exception.ConflictException;
import ru.semstore.userservice.exception.NotFoundException;
import ru.semstore.userservice.mapper.AddressMapper;
import ru.semstore.userservice.model.Address;
import ru.semstore.userservice.model.User;
import ru.semstore.userservice.repository.AddressRepository;
import ru.semstore.userservice.repository.UserRepository;
import ru.semstore.userservice.service.AddressService;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    private final byte MAX_USER_ADDRESSES_COUNT = 10;

    @Override
    public AddressDto create(AddressCreateDto dto, UUID userId) {
        User user = findUserByIdOrThrow(userId);
        List<Address> userAddresses = addressRepository.findAllByUserId(user.getId());
        if (userAddresses.size() >= MAX_USER_ADDRESSES_COUNT) {
            throw new ConflictException("User can have only 10 addresses");
        }
        Address address = addressMapper.toEntity(dto);
        address.setUser(user);
        Address savedAddress = addressRepository.save(address);
        log.debug("User id={} saved address with id={}", savedAddress.getId(), savedAddress.getUser().getId());
        return addressMapper.toDto(savedAddress);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AddressDto> getUserAddresses(UUID userId) {
        User user = findUserByIdOrThrow(userId);
        List<Address> userAddresses = addressRepository.findAllByUserId(user.getId());
        log.debug("Found user addresses. userId={}", user.getId());
        return addressMapper.listToDto(userAddresses);
    }

    @Override
    public AddressDto update(AddressUpdateDto dto, UUID addressId) {
        Address address = findAddressByIdOrThrow(addressId);
        addressMapper.updateFromDto(dto, address);
        addressRepository.save(address);
        log.debug("User updated address. userId={}, addressId={}", address.getUser().getId(), address.getId());
        return addressMapper.toDto(address);
    }

    @Transactional(readOnly = true)
    @Override
    public AddressDto getById(UUID addressId) {
        Address address = findAddressByIdOrThrow(addressId);
        log.debug("Found address with id={}", addressId);
        return addressMapper.toDto(address);
    }

    @Override
    public void delete(UUID addressId) {
        findAddressByIdOrThrow(addressId);
        addressRepository.deleteById(addressId);
        log.debug("Deleted address with id={}", addressId);
    }

    private User findUserByIdOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> {
            log.warn("User with id={} not found", id);
            return new NotFoundException("User not found");
        });
    }

    private Address findAddressByIdOrThrow(UUID id) {
        return addressRepository.findById(id).orElseThrow(() -> {
            log.warn("Address with id={} not found", id);
            return new NotFoundException("Address not found");
        });
    }
}
