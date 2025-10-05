package ru.semstore.userservice.mapper;

import org.mapstruct.*;
import ru.semstore.userservice.dto.address.AddressCreateDto;
import ru.semstore.userservice.dto.address.AddressDto;
import ru.semstore.userservice.dto.address.AddressUpdateDto;
import ru.semstore.userservice.model.Address;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface AddressMapper {

    @Mapping(target = "user", ignore = true)
    Address toEntity(AddressCreateDto dto);

    AddressDto toDto(Address entity);

    List<AddressDto> listToDto(List<Address> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateFromDto(AddressUpdateDto dto, @MappingTarget Address entity);
}
