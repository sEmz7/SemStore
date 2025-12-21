package ru.semstore.orderservice.orderItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.semstore.orderservice.dto.orderItem.OrderItemCreateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemUpdateDto;
import ru.semstore.orderservice.model.OrderItem;
import ru.semstore.orderservice.repository.OrderItemRepository;
import ru.semstore.orderservice.service.OrderItemService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class OrderItemServiceIT {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderItemRepository itemRepository;

    private UUID userId;
    private UUID orderId;
    private UUID itemId;
    private String link;
    private String size;
    private String configuration;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        orderId = UUID.fromString("08acd0a9-b1f8-40ab-a69a-efec2ab7fda6");
        itemId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        link = "link";
        size = "size";
        configuration = "configuration";
    }

    @Test
    @DisplayName("Создание товара в заказе")
    @Sql({"/data/cleanUp.sql", "/data/insert.sql"})
    void addItem_ShouldSave() {
        OrderItemCreateDto dto = new OrderItemCreateDto(link, size, configuration);

        OrderItemDto itemDto = orderItemService.addItem(userId, orderId, dto);

        List<OrderItem> items = itemRepository.findAll();
        assertEquals(1, items.size());

        OrderItem item = items.getFirst();
        assertNotNull(item.getId());
        assertNotNull(item.getOrder());
        assertEquals(orderId, item.getOrder().getId());
        assertEquals(userId, item.getOrder().getUserId());

        assertNotNull(itemDto);
        assertEquals(item.getId(), itemDto.getId());
    }

    @Test
    @DisplayName("Получение товара по ID")
    @Sql({"/data/cleanUp.sql", "/data/insertOrderWithItem.sql"})
    void getItemById_ShouldReturn() {
        OrderItemDto itemDto = orderItemService.getItemById(userId, orderId, itemId);

        assertNotNull(itemDto);
        assertEquals(itemId, itemDto.getId());
        assertEquals(orderId, itemDto.getOrderId());
    }

    @Test
    @DisplayName("Обновление товара в заказе")
    @Sql({"/data/cleanUp.sql", "/data/insertOrderWithItem.sql"})
    void update_ShouldUpdate() {
        OrderItemUpdateDto dto = new OrderItemUpdateDto(link, size, configuration);

        OrderItemDto updatedDto = orderItemService.update(userId, orderId, itemId, dto);

        itemRepository.findById(itemId)
                .orElseThrow(() -> new AssertionError("Item not found after update"));

        assertNotNull(updatedDto);
        assertEquals(itemId, updatedDto.getId());
        assertEquals(link, updatedDto.getLink());
        assertEquals(size, updatedDto.getSize());
        assertEquals(configuration, updatedDto.getConfiguration());
    }

    @Test
    @DisplayName("Удаление товара из заказа")
    @Sql({"/data/cleanUp.sql", "/data/insertOrderWithItem.sql"})
    void delete_ShouldDelete() {
        orderItemService.delete(userId, orderId, itemId);

        assertTrue(itemRepository.findById(itemId).isEmpty());
    }
}