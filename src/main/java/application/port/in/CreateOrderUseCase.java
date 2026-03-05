package application.port.in;

import java.util.UUID;

public interface CreateOrderUseCase {
    UUID createOrder(CreateOrderCommand command);
}