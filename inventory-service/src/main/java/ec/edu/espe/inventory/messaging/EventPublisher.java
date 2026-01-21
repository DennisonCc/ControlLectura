package ec.edu.espe.inventory.messaging;

import ec.edu.espe.inventory.config.RabbitMQConfig;
import ec.edu.espe.inventory.dto.StockRejectedEvent;
import ec.edu.espe.inventory.dto.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStockReserved(UUID orderId) {
        StockReservedEvent event = new StockReservedEvent(orderId);
        
        log.info("Publishing StockReserved event for order: {}", orderId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDERS_EXCHANGE,
                RabbitMQConfig.STOCK_RESERVED_ROUTING_KEY,
                event
        );
        log.info("StockReserved event published successfully");
    }

    public void publishStockRejected(UUID orderId, String reason) {
        StockRejectedEvent event = new StockRejectedEvent(orderId, reason);
        
        log.info("Publishing StockRejected event for order: {} - Reason: {}", orderId, reason);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDERS_EXCHANGE,
                RabbitMQConfig.STOCK_REJECTED_ROUTING_KEY,
                event
        );
        log.info("StockRejected event published successfully");
    }
}
