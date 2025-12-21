package ru.semstore.orderservice.mapper;

import org.mapstruct.*;
import ru.semstore.orderservice.dto.orderItem.OrderItemCreateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemUpdateDto;
import ru.semstore.orderservice.model.OrderItem;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderItemMapper {

    OrderItem toEntity(OrderItemCreateDto dto);

    @Mapping(target = "orderId", source = "order.id")
    OrderItemDto toDto(OrderItem entity);

    void update(@MappingTarget OrderItem item, OrderItemUpdateDto dto);
}
