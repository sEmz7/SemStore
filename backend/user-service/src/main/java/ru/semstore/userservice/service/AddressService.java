package ru.semstore.userservice.service;

import ru.semstore.userservice.dto.address.AddressCreateDto;
import ru.semstore.userservice.dto.address.AddressDto;
import ru.semstore.userservice.dto.address.AddressUpdateDto;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления адресами пользователя.
 */
public interface AddressService {

    /**
     * Создаёт новый адрес пользователя.
     *
     * @param dto    данные для создания адреса
     * @param userId идентификатор пользователя
     * @return созданный адрес
     */
    AddressDto create(AddressCreateDto dto, UUID userId);

    /**
     * Возвращает все адреса пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список адресов пользователя
     */
    List<AddressDto> getUserAddresses(UUID userId);

    /**
     * Обновляет существующий адрес.
     *
     * @param dto           данные для обновления адреса
     * @param addressId     идентификатор адреса
     * @param currentUserId идентификатор текущего пользователя
     * @return обновлённый адрес
     */
    AddressDto update(AddressUpdateDto dto, UUID addressId, UUID currentUserId);

    /**
     * Возвращает адрес по идентификатору.
     *
     * @param addressId идентификатор адреса
     * @return адрес
     */
    AddressDto getById(UUID addressId);

    /**
     * Удаляет адрес пользователя.
     *
     * @param addressId     идентификатор адреса
     * @param currentUserId идентификатор текущего пользователя
     */
    void delete(UUID addressId, UUID currentUserId);
}
