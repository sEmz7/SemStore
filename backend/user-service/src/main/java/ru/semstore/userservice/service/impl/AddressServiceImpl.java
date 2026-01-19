package ru.semstore.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.userservice.dto.address.AddressCreateDto;
import ru.semstore.userservice.dto.address.AddressDto;
import ru.semstore.userservice.dto.address.AddressUpdateDto;
import ru.semstore.userservice.exception.ConflictException;
import ru.semstore.userservice.exception.ErrorCode;
import ru.semstore.userservice.exception.NotFoundException;
import ru.semstore.userservice.mapper.AddressMapper;
import ru.semstore.userservice.model.Address;
import ru.semstore.userservice.model.User;
import ru.semstore.userservice.repository.AddressRepository;
import ru.semstore.userservice.repository.UserRepository;
import ru.semstore.userservice.service.AddressService;

import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса управления адресами пользователя.
 *
 * <p>Содержит бизнес-логику проверки владельца адреса
 * и ограничения на максимальное количество адресов.</p>
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    private final byte MAX_USER_ADDRESSES_COUNT = 10;

    /**
     * Создаёт новый адрес для пользователя.
     *
     * @param dto    данные для создания адреса
     * @param userId идентификатор пользователя
     * @return созданный адрес
     * @throws NotFoundException если пользователь не найден
     * @throws ConflictException если превышено максимальное количество адресов
     */
    @Override
    public AddressDto create(AddressCreateDto dto, UUID userId) {
        User user = findUserByIdOrThrow(userId);
        List<Address> userAddresses = addressRepository.findAllByUserId(user.getId());
        if (userAddresses.size() >= MAX_USER_ADDRESSES_COUNT) {
            throw new ConflictException("User can have only 10 addresses", ErrorCode.ADDRESS_COUNT_LIMIT);
        }
        Address address = addressMapper.toEntity(dto);
        address.setUser(user);
        Address savedAddress = addressRepository.save(address);
        log.debug("User id={} saved address with id={}", savedAddress.getId(), savedAddress.getUser().getId());
        return addressMapper.toDto(savedAddress);
    }

    /**
     * Возвращает список адресов пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список адресов
     * @throws NotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    @Override
    public List<AddressDto> getUserAddresses(UUID userId) {
        User user = findUserByIdOrThrow(userId);
        List<Address> userAddresses = addressRepository.findAllByUserId(user.getId());
        log.debug("Found user addresses. userId={}", user.getId());
        return addressMapper.listToDto(userAddresses);
    }

    /**
     * Обновляет существующий адрес пользователя.
     *
     * @param dto            данные для обновления адреса
     * @param addressId      идентификатор адреса
     * @param currentUserId  идентификатор текущего пользователя
     * @return обновлённый адрес
     * @throws NotFoundException если адрес не найден
     * @throws ConflictException если пользователь не является владельцем адреса
     */
    @Override
    public AddressDto update(AddressUpdateDto dto, UUID addressId, UUID currentUserId) {
        Address address = findAddressByIdOrThrow(addressId);
        if (!address.getUser().getId().equals(currentUserId)) {
            throw new ConflictException("Only owner can change their addresses", ErrorCode.ADDRESS_OWNER_CONFLICT);
        }
        addressMapper.updateFromDto(dto, address);
        addressRepository.save(address);
        log.debug("User updated address. userId={}, addressId={}", address.getUser().getId(), address.getId());
        return addressMapper.toDto(address);
    }

    /**
     * Возвращает адрес по идентификатору.
     *
     * @param addressId идентификатор адреса
     * @return адрес
     * @throws NotFoundException если адрес не найден
     */
    @Transactional(readOnly = true)
    @Override
    public AddressDto getById(UUID addressId) {
        Address address = findAddressByIdOrThrow(addressId);
        log.debug("Found address with id={}", addressId);
        return addressMapper.toDto(address);
    }

    /**
     * Удаляет адрес пользователя.
     *
     * @param addressId     идентификатор адреса
     * @param currentUserId идентификатор текущего пользователя
     * @throws NotFoundException если адрес не найден
     * @throws ConflictException если пользователь не является владельцем адреса
     */
    @Override
    public void delete(UUID addressId, UUID currentUserId) {
        Address address = findAddressByIdOrThrow(addressId);
        if (!address.getUser().getId().equals(currentUserId)) {
            throw new ConflictException("Only owner can delete their addresses", ErrorCode.ADDRESS_OWNER_CONFLICT);
        }
        addressRepository.deleteById(addressId);
        log.debug("Deleted address with id={}", addressId);
    }

    private User findUserByIdOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> {
            log.warn("User with id={} not found", id);
            return new NotFoundException("User not found", ErrorCode.USER_NOT_FOUND);
        });
    }

    private Address findAddressByIdOrThrow(UUID id) {
        return addressRepository.findById(id).orElseThrow(() -> {
            log.warn("Address with id={} not found", id);
            return new NotFoundException("Address not found", ErrorCode.ADDRESS_NOT_FOUND);
        });
    }
}
