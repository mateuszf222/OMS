package org.example.paymentservice.infrastructure.adapter.out.persistence;

import org.example.paymentservice.domain.model.Money;
import org.example.paymentservice.domain.model.Payment;
import org.example.paymentservice.domain.model.PaymentState;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface PaymentEntityMapper {

    @Mapping(target = "amount", source = "amount", qualifiedByName = "extractAmount")
    @Mapping(target = "currency", source = "amount", qualifiedByName = "extractCurrency")
    PaymentJpaEntity toJpaEntity(Payment payment);

    @Named("extractAmount")
    default BigDecimal extractAmount(Money money) {
        return money.amount();
    }

    @Named("extractCurrency")
    default String extractCurrency(Money money) {
        return money.currency();
    }

    default Payment toDomainModel(PaymentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Money amount = Money.of(entity.getAmount(), entity.getCurrency());

        return Payment.restore(new PaymentState(
                entity.getId(),
                entity.getOrderId(),
                amount,
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getCustomerId()
        ));
    }
}