package ru.semstore.orderservice.order;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.semstore.orderservice.dto.order.OrderCreateDto;
import ru.semstore.orderservice.dto.order.OrderFullDto;
import ru.semstore.orderservice.dto.order.OrderShortDto;
import ru.semstore.orderservice.dto.order.OrderUpdateDto;
import ru.semstore.orderservice.errors.exceptions.ConflictException;
import ru.semstore.orderservice.errors.exceptions.OrderNotFoundException;
import ru.semstore.orderservice.kafka.producer.KafkaProducer;
import ru.semstore.orderservice.mapper.OrderMapper;
import ru.semstore.orderservice.model.Order;
import ru.semstore.orderservice.model.OrderItem;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.repository.OrderRepository;
import ru.semstore.orderservice.service.impl.OrderServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private KafkaProducer kafka;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID userId;
    private final String name = "name";
    private UUID orderId;
    private UUID addressId;
    private Order order;
    private OrderShortDto orderDto;
    private OrderFullDto orderFullDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        addressId = UUID.randomUUID();
        order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedDate(LocalDateTime.now());
        order.setItems(List.of(new OrderItem(UUID.randomUUID(), order, "link",
                        "size", "configuration", null)));

        orderDto = new OrderShortDto(orderId, name, userId, addressId, OrderStatus.PENDING, null);
        orderFullDto = new OrderFullDto(orderId, name, userId, addressId, OrderStatus.PENDING, null, null);
    }

    @Test
    @DisplayName("Создание заказа")
    void create_ShouldSave() {
        OrderCreateDto dto = new OrderCreateDto(name, addressId);
        when(orderMapper.toEntity(dto)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toShortDto(order)).thenReturn(orderDto);

        OrderShortDto result = orderService.create(dto, userId);

        assertNotNull(result);
        assertEquals(result.getId(), orderId);
        verify(orderRepository, times(1)).save(order);
        verify(kafka, times(1)).sendOrderToCheck(order);
    }

    @Test
    @DisplayName("Обновление заказа")
    void update_ShouldUpdateOrder() {
        OrderUpdateDto dto = new OrderUpdateDto(name, addressId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toShortDto(order)).thenReturn(orderDto);

        OrderShortDto result = orderService.update(dto, orderId, userId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(orderRepository, times(1)).save(order);
        verify(kafka, times(1)).sendOrderToCheck(order);
    }

    @Test
    @DisplayName("Обновление заказа со статусом PAID")
    void update_ShouldThrow_WhenStatusPaid() {
        OrderUpdateDto dto = new OrderUpdateDto(name, addressId);
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(ConflictException.class, () -> orderService.update(dto, orderId, userId));
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Обновление заказа со статусом IN_CHECK")
    void update_ShouldThrow_WhenStatusOrdered() {
        OrderUpdateDto dto = new OrderUpdateDto(name, addressId);
        order.setStatus(OrderStatus.IN_CHECK);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(ConflictException.class, () -> orderService.update(dto, orderId, userId));
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Удаление заказа")
    void delete_ShouldDelete() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.delete(orderId, userId);

        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @Test
    @DisplayName("Удаление заказа не его создателем")
    void delete_ShouldThrow_WhenUserNotOwner() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(ConflictException.class, () -> orderService.delete(orderId, UUID.randomUUID()));

        verify(orderRepository, times(0)).deleteById(orderId);
    }

    @Test
    @DisplayName("Удаление заказа со статусом PAID")
    void delete_shouldThrow_WhenOrderStatusPaid() {
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(ConflictException.class, () -> orderService.delete(orderId, userId));

        verify(orderRepository, times(0)).deleteById(orderId);
    }

    @Test
    @DisplayName("Удаление заказа со статусом IN_CHECK")
    void delete_shouldThrow_WhenOrderStatusOrdered() {
        order.setStatus(OrderStatus.IN_CHECK);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(ConflictException.class, () -> orderService.delete(orderId, userId));

        verify(orderRepository, times(0)).deleteById(orderId);
    }

    @Test
    @DisplayName("Получение заказа по ID")
    void getById_ShouldReturnOrder() {
        when(orderRepository.findOrderByIdWithItems(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toFullDto(order)).thenReturn(orderFullDto);

        OrderFullDto result = orderService.getById(orderId, userId);

        assertNotNull(result);
        assertEquals(orderId, result.id());
        verify(orderRepository, times(1)).findOrderByIdWithItems(orderId);
    }

    @Test
    @DisplayName("Получение заказа по ID не владельцем")
    void getById_ShouldThrow_WhenUserNotOwner() {
        when(orderRepository.findOrderByIdWithItems(orderId)).thenReturn(Optional.of(order));

        assertThrows(ConflictException.class, () -> orderService.getById(orderId, UUID.randomUUID()));

        verify(orderRepository, times(1)).findOrderByIdWithItems(orderId);
    }

    @Test
    @DisplayName("Получение несуществующего заказа по ID ")
    void getById_ShouldThrow_WhenOrderNotCreated() {
        when(orderRepository.findOrderByIdWithItems(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getById(orderId, userId));

        verify(orderRepository, times(1)).findOrderByIdWithItems(orderId);
    }

    @Test
    @DisplayName("Получение всех заказов пользователя")
    void getAll_ShouldReturn() {
        when(orderRepository.findAllBySortAndUserId(
                any(Pageable.class), eq(userId), eq(OrderStatus.PENDING), isNull(), isNull()
        )).thenReturn(new PageImpl<>(List.of(order)));

        when(orderMapper.toShortDto(any())).thenReturn(orderDto);

        List<OrderShortDto> result =
                orderService.getAll(userId, 0, 10, OrderStatus.PENDING, null, null)
                        .content();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderId, result.getFirst().getId());
    }

    @Test
    @DisplayName("Подтверждение заказа пользователем")
    void confirmOrder_ShouldConfirm() {
        order.setStatus(OrderStatus.CREATED);
        when(orderRepository.findOrderByIdWithItems(orderId)).thenReturn(Optional.of(order));

        OrderFullDto orderFullDto = new OrderFullDto(orderId, name, userId, addressId, OrderStatus.IN_CHECK,
                null, null);
        when(orderMapper.toFullDto(any())).thenReturn(orderFullDto);

        OrderFullDto dto = orderService.confirmOrder(orderId, userId);

        assertNotNull(dto);

        verify(orderRepository, times(1)).findOrderByIdWithItems(orderId);
        verify(orderMapper, times(1)).toFullDto(order);
        assertEquals(OrderStatus.IN_CHECK, dto.status());
    }
}