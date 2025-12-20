package ru.semstore.orderservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.semstore.orderservice.dto.orderItem.OrderItemCreateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.model.OrderItem;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    OrderItem toEntity(OrderItemCreateDto dto);

    @Mapping(target = "orderId", source = "order.id")
    OrderItemDto toDto(OrderItem entity);
}
