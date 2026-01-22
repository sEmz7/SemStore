package ru.semstore.userservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.semstore.common.dto.OrderCheckedEvent;
import ru.semstore.common.dto.OrderCreatedEvent;
import ru.semstore.userservice.kafka.producer.KafkaProducer;
import ru.semstore.userservice.repository.AddressRepository;
import ru.semstore.userservice.repository.UserRepository;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final KafkaProducer kafka;

    @KafkaListener(topics = "order-created")
    public void listenCreatedOrder(OrderCreatedEvent event) {
        log.debug("Order received for check, orderId={}", event.getOrderId());
        boolean userExists = userRepository.existsById(event.getUserId());
        boolean addressExists = addressRepository.existsById(event.getAddressId());
        log.debug("User exists={}, address exists={}", userExists, addressExists);

        var checkedOrder = new OrderCheckedEvent(event.getOrderId(), userExists && addressExists);
        kafka.sendCheckedOrder(checkedOrder);
    }

    @KafkaListener(topics = "order-completed")
    public void listenCompletedOrder(String payload) {
        log.debug("Address id received for complete, addressId={}", payload);
        UUID addressId;
        try {
            addressId = UUID.fromString(payload);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid addressId payload: {}", payload);
            return;
        }

        addressRepository.findById(addressId).ifPresent(address -> {
            if (address.isDeleted()) {
                addressRepository.deleteById(address.getId());
            }
        });
    }
}
