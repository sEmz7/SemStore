package ru.semstore.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.semstore.userservice.model.Address;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findAllByUserId(UUID id);
}
