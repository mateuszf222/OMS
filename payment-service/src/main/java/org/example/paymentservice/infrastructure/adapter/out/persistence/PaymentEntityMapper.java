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

    @Mapping(target = "amount", source = "amount", qualifiedByName = "moneyToAmount")
    @Mapping(target = "currency", source = "amount", qualifiedByName = "moneyToCurrency")
    PaymentJpaEntity toJpaEntity(Payment payment);

    @Named("moneyToAmount")
    default BigDecimal moneyToAmount(Money money) {
        return money.amount();
    }

    @Named("moneyToCurrency")
    default String moneyToCurrency(Money money) {
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
                entity.getCreatedAt())
        );
    }
}