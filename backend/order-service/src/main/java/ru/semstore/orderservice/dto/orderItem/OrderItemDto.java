package ru.semstore.orderservice.dto.orderItem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "DTO для просмотра товара в заказе")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    @Schema(description = "ID товара", example = "5526ff00-fe2c-4191-9d35-6d5acf537869")
    private UUID id;

    @Schema(description = "ID заказа", example = "5526ff00-fe2c-4191-9d35-6d5acf537869")
    private UUID orderId;

    @Schema(description = "Ссылка на товар", example = "https://dw4.co/t/A/1tpEiZDfq")
    private String link;

    @Schema(description = "Размер товара", example = "42.5")
    private String size;

    @Schema(description = "Конфигурация товара", example = "черные")
    private String configuration;

    @Schema(description = "Цена товара", example = "9790.90")
    private BigDecimal price;
}
