package org.example.paymentservice.infrastructure.adapter.out.persistence;

import org.example.paymentservice.domain.model.Payment;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface PaymentEntityMapper {

    PaymentJpaEntity toJpaEntity(Payment payment);

    default Payment toDomainModel(PaymentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Payment.restore(
                entity.getId(),
                entity.getOrderId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}