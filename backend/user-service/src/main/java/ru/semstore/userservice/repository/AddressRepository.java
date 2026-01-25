package ru.semstore.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.semstore.userservice.model.Address;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findAllByUserIdAndDeleted(UUID id, boolean deleted);

    @Query("SELECT COUNT(a) > 0 FROM Address a " +
            "WHERE a.id = :addressId " +
            "AND a.deleted = false " +
            "AND a.user.id = :userId")
    boolean existsActiveOwned(@Param("addressId") UUID addressId, @Param("userId") UUID userId);
}
