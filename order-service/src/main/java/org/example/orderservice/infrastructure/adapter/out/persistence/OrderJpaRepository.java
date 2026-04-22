package org.example.orderservice.infrastructure.adapter.out.persistence;

// ... existing code ...
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    @Query("SELECT o FROM OrderJpaEntity o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderJpaEntity> findByIdWithItems(@Param("id") UUID id);
}