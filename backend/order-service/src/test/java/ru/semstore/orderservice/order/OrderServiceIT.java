package ru.semstore.orderservice.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.semstore.orderservice.dto.order.OrderCreateDto;
import ru.semstore.orderservice.dto.order.OrderFullDto;
import ru.semstore.orderservice.dto.order.OrderShortDto;
import ru.semstore.orderservice.dto.order.OrderUpdateDto;
import ru.semstore.orderservice.kafka.producer.KafkaProducer;
import ru.semstore.orderservice.model.Order;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.repository.OrderRepository;
import ru.semstore.orderservice.service.OrderService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class OrderServiceIT {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private KafkaProducer kafka;

    private UUID userId;
    private final String name = "name";
    private UUID addressId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        addressId = UUID.randomUUID();
        orderId = UUID.fromString("08acd0a9-b1f8-40ab-a69a-efec2ab7fda6");
    }

    @Test
    @DisplayName("Создание заказа")
    @Sql("/data/cleanUp.sql")
    void create_ShouldSave() {
        OrderCreateDto dto = new OrderCreateDto(name, addressId);

        orderService.create(dto, userId);

        Order order = orderRepository.findAll().getFirst();

        assertNotNull(order);
        assertEquals(dto.addressId(), order.getAddressId());
        assertEquals(userId, order.getUserId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    @DisplayName("Обновление заказа")
    @Sql({"/data/cleanUp.sql", "/data/insert.sql"})
    void update_ShouldUpdate() {
        OrderUpdateDto dto = new OrderUpdateDto(name, UUID.fromString("08acd0a9-b1f8-40ab-a69a-efec2ab7fda6"));

        orderService.update(dto, orderId);

        Order order = orderRepository.findAll().getFirst();

        assertNotNull(order);
        assertEquals(dto.addressId(), order.getAddressId());
        assertEquals(orderId, order.getId());
    }

    @Test
    @DisplayName("Удаление заказа")
    @Sql({"/data/cleanUp.sql", "/data/insert.sql"})
    void delete_ShouldDelete() {
        orderService.delete(orderId, userId);

        List<Order> orders = orderRepository.findAll();

        assertNotNull(orders);
        assertEquals(0, orders.size());
    }

    @Test
    @DisplayName("Получение заказа по ID")
    @Sql({"/data/cleanUp.sql", "/data/insert.sql"})
    void getById_ShouldReturn() {
        OrderFullDto orderDto = orderService.getById(orderId, userId);

        assertNotNull(orderDto);
        assertEquals(orderId, orderDto.id());
    }

    @Test
    @DisplayName("Получение всех заказов пользователя")
    @Sql({"/data/cleanUp.sql", "/data/insertAll.sql"})
    void getAll_ShouldReturn() {
        List<OrderShortDto> orderDtos = orderService
                .getAll(userId, 0, 10, OrderStatus.PENDING, null, null).content();

        assertNotNull(orderDtos);
        assertEquals(10, orderDtos.size());
        assertTrue(orderDtos.stream().allMatch(orderDto -> userId.equals(orderDto.getUserId())));
    }
}
