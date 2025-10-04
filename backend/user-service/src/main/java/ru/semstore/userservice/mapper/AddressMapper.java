package ru.semstore.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.semstore.userservice.dto.address.AddressCreateDto;
import ru.semstore.userservice.dto.address.AddressDto;
import ru.semstore.userservice.model.Address;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface AddressMapper {

    @Mapping(target = "user", ignore = true)
    Address toEntity(AddressCreateDto dto);

    AddressDto toDto(Address entity);
}
