package ec.edu.espe.order_service.listener;

import ec.edu.espe.order_service.config.RabbitMQConfig;
import ec.edu.espe.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_RESPONSE_QUEUE)
    public void handleInventoryResponse(Map<String, Object> event) {
        log.info("Received event: {}", event);
        String eventType = (String) event.get("eventType");
        String orderId = (String) event.get("orderId");

        if ("StockReserved".equals(eventType)) {
            orderService.confirmOrder(orderId);
        } else if ("StockRejected".equals(eventType)) {
            String reason = (String) event.get("reason");
            orderService.cancelOrder(orderId, reason);
        } else {
            log.warn("Unknown event type: {}", eventType);
        }
    }
}
