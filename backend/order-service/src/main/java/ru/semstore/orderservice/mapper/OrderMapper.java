
package ru.semstore.orderservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.semstore.orderservice.dto.OrderCreateDto;
import ru.semstore.orderservice.dto.OrderDto;
import ru.semstore.orderservice.model.Order;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    Order toEntity(OrderCreateDto dto);

    OrderDto toDto(Order entity);
}
