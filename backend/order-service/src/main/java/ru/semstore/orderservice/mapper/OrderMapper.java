
package ru.semstore.orderservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.semstore.orderservice.dto.order.OrderCreateDto;
import ru.semstore.orderservice.dto.order.OrderFullDto;
import ru.semstore.orderservice.dto.order.OrderShortDto;
import ru.semstore.orderservice.model.Order;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    Order toEntity(OrderCreateDto dto);

    OrderShortDto toShortDto(Order entity);

    OrderFullDto toFullDto(Order entity);
}
