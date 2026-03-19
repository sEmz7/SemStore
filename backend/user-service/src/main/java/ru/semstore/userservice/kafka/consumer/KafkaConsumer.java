package ru.semstore.userservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.semstore.common.dto.OrderCheckedEvent;
import ru.semstore.common.dto.OrderCreatedEvent;
import ru.semstore.userservice.kafka.producer.KafkaProducer;
import ru.semstore.userservice.model.User;
import ru.semstore.userservice.repository.AddressRepository;
import ru.semstore.userservice.repository.UserRepository;

import java.util.Optional;
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
        Optional<User> user = userRepository.findById(event.getUserId());
        boolean userExists = user.isPresent();
        boolean addressExistsAndNotDeletedAndOwned =
                addressRepository.existsActiveOwned(
                        event.getAddressId(),
                        event.getUserId()
                );
        log.debug("User exists={}, address exists={}", userExists, addressExistsAndNotDeletedAndOwned);
        String email = user.map(User::getEmail).orElse(null);
        var checkedOrder = new OrderCheckedEvent(
                event.getOrderId(),
                userExists && addressExistsAndNotDeletedAndOwned,
                email,
                event.getTrackingNumber()
        );
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
