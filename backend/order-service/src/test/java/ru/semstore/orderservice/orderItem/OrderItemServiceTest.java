package ru.semstore.orderservice.orderItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.semstore.orderservice.dto.orderItem.OrderItemCreateDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemDto;
import ru.semstore.orderservice.dto.orderItem.OrderItemUpdateDto;
import ru.semstore.orderservice.errors.exceptions.ConflictException;
import ru.semstore.orderservice.errors.exceptions.ItemNotFoundException;
import ru.semstore.orderservice.errors.exceptions.OrderNotFoundException;
import ru.semstore.orderservice.mapper.OrderItemMapper;
import ru.semstore.orderservice.model.Order;
import ru.semstore.orderservice.model.OrderItem;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.repository.OrderItemRepository;
import ru.semstore.orderservice.repository.OrderRepository;
import ru.semstore.orderservice.service.impl.OrderItemServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderItemServiceTest {
    @Mock
    private OrderItemRepository itemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemMapper itemMapper;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private UUID userId;
    private UUID orderId;
    private UUID otherOrderId;
    private UUID itemId;

    private Order order;
    private Order otherOrder;
    private OrderItem item;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        otherOrderId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);

        otherOrder = new Order();
        otherOrder.setId(otherOrderId);
        otherOrder.setUserId(userId);
        otherOrder.setStatus(OrderStatus.PENDING);

        item = new OrderItem();
        item.setId(itemId);
        item.setOrder(order);
    }

    @Test
    @DisplayName("addItem: успешное добавление товара к заказу")
    void addItem_ShouldSave_WhenOrderExistsAndUserOwnerAndOrderModifiable() {
        OrderItemCreateDto dto = mock(OrderItemCreateDto.class);
        OrderItemDto itemDto = mock(OrderItemDto.class);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(itemMapper.toEntity(dto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        OrderItemDto result = orderItemService.addItem(userId, orderId, dto);

        assertNotNull(result);
        assertSame(itemDto, result);
        assertEquals(order, item.getOrder());

        verify(orderRepository, times(1)).findById(orderId);
        verify(itemMapper, times(1)).toEntity(dto);
        verify(itemRepository, times(1)).save(item);
        verify(itemMapper, times(1)).toDto(item);
        verifyNoMoreInteractions(orderRepository, itemRepository, itemMapper);
    }

    @Test
    @DisplayName("addItem: заказ не найден -> OrderNotFoundException")
    void addItem_ShouldThrow_WhenOrderNotFound() {
        OrderItemCreateDto dto = mock(OrderItemCreateDto.class);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderItemService.addItem(userId, orderId, dto));

        verify(orderRepository, times(1)).findById(orderId);
        verifyNoMoreInteractions(orderRepository, itemRepository, itemMapper);
    }

    @Test
    @DisplayName("addItem: заказ в статусе PAID/ORDERED/CANCELED -> ConflictException")
    void addItem_ShouldThrow_WhenOrderNotModifiable() {
        Order notModifiableOrder = new Order();
        notModifiableOrder.setId(orderId);
        notModifiableOrder.setUserId(userId);
        notModifiableOrder.setStatus(OrderStatus.PAID);

        OrderItemCreateDto dto = mock(OrderItemCreateDto.class);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(notModifiableOrder));

        assertThrows(ConflictException.class,
                () -> orderItemService.addItem(userId, orderId, dto));

        verify(orderRepository, times(1)).findById(orderId);
        verifyNoMoreInteractions(orderRepository, itemRepository, itemMapper);
    }

    @Test
    @DisplayName("getItemById: успешное получение товара")
    void getItemById_ShouldReturn_WhenUserOwnerAndItemBelongsToOrder() {
        OrderItemDto dto = mock(OrderItemDto.class);

        when(itemRepository.findItemByIdWithOrder(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(dto);

        OrderItemDto result = orderItemService.getItemById(userId, orderId, itemId);

        assertNotNull(result);
        assertSame(dto, result);

        verify(itemRepository, times(1)).findItemByIdWithOrder(itemId);
        verify(itemMapper, times(1)).toDto(item);
        verifyNoMoreInteractions(itemRepository, orderRepository, itemMapper);
    }

    @Test
    @DisplayName("getItemById: товар не найден -> ItemNotFoundException")
    void getItemById_ShouldThrow_WhenItemNotFound() {
        when(itemRepository.findItemByIdWithOrder(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> orderItemService.getItemById(userId, orderId, itemId));

        verify(itemRepository, times(1)).findItemByIdWithOrder(itemId);
        verifyNoMoreInteractions(itemRepository, orderRepository, itemMapper);
    }

    @Test
    @DisplayName("getItemById: товар принадлежит другому заказу -> ConflictException")
    void getItemById_ShouldThrow_WhenItemBelongsToAnotherOrder() {
        item.setOrder(otherOrder);
        when(itemRepository.findItemByIdWithOrder(itemId)).thenReturn(Optional.of(item));

        assertThrows(ConflictException.class,
                () -> orderItemService.getItemById(userId, orderId, itemId));

        verify(itemRepository, times(1)).findItemByIdWithOrder(itemId);
        verifyNoMoreInteractions(itemRepository, orderRepository, itemMapper);
    }

    @Test
    @DisplayName("delete: успешное удаление товара")
    void delete_ShouldDelete_WhenUserOwnerAndOrderModifiable() {
        when(itemRepository.findItemByIdWithOrder(itemId)).thenReturn(Optional.of(item));

        orderItemService.delete(userId, orderId, itemId);

        verify(itemRepository, times(1)).findItemByIdWithOrder(itemId);
        verify(itemRepository, times(1)).deleteById(itemId);
        verifyNoMoreInteractions(itemRepository, orderRepository, itemMapper);
    }

    @Test
    @DisplayName("delete: товар не найден -> ItemNotFoundException")
    void delete_ShouldThrow_WhenItemNotFound() {
        when(itemRepository.findItemByIdWithOrder(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> orderItemService.delete(userId, orderId, itemId));

        verify(itemRepository, times(1)).findItemByIdWithOrder(itemId);
        verifyNoMoreInteractions(itemRepository, orderRepository, itemMapper);
    }

    @Test
    @DisplayName("delete: заказ в статусе PAID/ORDERED/CANCELED -> ConflictException")
    void delete_ShouldThrow_WhenOrderNotModifiable() {
        order.setStatus(OrderStatus.CANCELED);
        when(itemRepository.findItemByIdWithOrder(itemId)).thenReturn(Optional.of(item));

        assertThrows(ConflictException.class,
                () -> orderItemService.delete(userId, orderId, itemId));

        verify(itemRepository, times(1)).findItemByIdWithOrder(itemId);
        verifyNoMoreInteractions(itemRepository, orderRepository, itemMapper);
    }

    @Test
    @DisplayName("update: успешное обновление товара")
    void update_ShouldUpdate_WhenUserOwnerAndOrderModifiable() {
        OrderItemUpdateDto updateDto = mock(OrderItemUpdateDto.class);
        OrderItemDto resultDto = mock(OrderItemDto.class);

        when(itemRepository.findItemByIdWithOrder(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(resultDto);

        OrderItemDto result = orderItemService.update(userId, orderId, itemId, updateDto);

        assertNotNull(result);
        assertSame(resultDto, result);

        verify(itemRepository, times(1)).findItemByIdWithOrder(itemId);
        verify(itemMapper, times(1)).update(item, updateDto);
        verify(itemRepository, times(1)).save(item);
        verify(itemMapper, times(1)).toDto(item);
        verifyNoMoreInteractions(itemRepository, orderRepository, itemMapper);
    }

    @Test
    @DisplayName("update: товар не найден -> ItemNotFoundException")
    void update_ShouldThrow_WhenItemNotFound() {
        OrderItemUpdateDto updateDto = mock(OrderItemUpdateDto.class);
        when(itemRepository.findItemByIdWithOrder(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> orderItemService.update(userId, orderId, itemId, updateDto));

        verify(itemRepository, times(1)).findItemByIdWithOrder(itemId);
        verifyNoMoreInteractions(itemRepository, orderRepository, itemMapper);
    }

    @Test
    @DisplayName("update: заказ в статусе PAID/ORDERED/CANCELED -> ConflictException")
    void update_ShouldThrow_WhenOrderNotModifiable() {
        order.setStatus(OrderStatus.PAID);
        OrderItemUpdateDto updateDto = mock(OrderItemUpdateDto.class);
        when(itemRepository.findItemByIdWithOrder(itemId)).thenReturn(Optional.of(item));

        assertThrows(ConflictException.class,
                () -> orderItemService.update(userId, orderId, itemId, updateDto));

        verify(itemRepository, times(1)).findItemByIdWithOrder(itemId);
        verifyNoMoreInteractions(itemRepository, orderRepository, itemMapper);
    }
}