package org.example.orderservice.infrastructure.adapter.out.persistence.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    // Pessimistic Write blokuje wybrane wiersze na poziomie bazy danych (FOR UPDATE),
    // dzięki czemu inne instancje serwisu ich nie pobiorą dopóki transakcja trwa.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM OutboxEventJpaEntity e WHERE e.processed = false ORDER BY e.createdAt ASC LIMIT 50")
    List<OutboxEventJpaEntity> findTop50UnprocessedEvents();
}