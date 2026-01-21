package ec.edu.espe.inventory.messaging;

import ec.edu.espe.inventory.config.RabbitMQConfig;
import ec.edu.espe.inventory.dto.OrderCreatedEvent;
import ec.edu.espe.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final InventoryService inventoryService;
    private final EventPublisher eventPublisher;

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_ORDERS_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreated event: orderId={}, customerId={}, items={}", 
                event.getOrderId(), event.getCustomerId(), event.getItems().size());

        try {
            // Verificar disponibilidad de stock
            boolean stockAvailable = inventoryService.checkStockAvailability(event.getItems());

            if (stockAvailable) {
                // Reservar stock
                inventoryService.reserveStock(event.getOrderId(), event.getItems());
                
                // Publicar evento de éxito
                eventPublisher.publishStockReserved(event.getOrderId());
                log.info("Stock reserved successfully for order: {}", event.getOrderId());
            } else {
                // Encontrar el producto problemático
                UUID problematicProductId = inventoryService.findFirstInsufficientStockProduct(event.getItems());
                String reason = "Insufficient stock for product " + problematicProductId;
                
                // Publicar evento de rechazo
                eventPublisher.publishStockRejected(event.getOrderId(), reason);
                log.warn("Stock reservation rejected for order: {} - {}", event.getOrderId(), reason);
            }
        } catch (Exception e) {
            log.error("Error processing OrderCreated event for order: {}", event.getOrderId(), e);
            eventPublisher.publishStockRejected(event.getOrderId(), 
                    "Error processing order: " + e.getMessage());
        }
    }
}
