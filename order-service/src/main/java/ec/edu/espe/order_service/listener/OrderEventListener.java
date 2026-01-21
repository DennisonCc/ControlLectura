package ec.edu.espe.order_service.listener;

import ec.edu.espe.order_service.config.RabbitMQConfig;
import ec.edu.espe.order_service.dto.StockRejectedEvent;
import ec.edu.espe.order_service.dto.StockReservedEvent;
import ec.edu.espe.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.ORDERS_RESULTS_QUEUE)
    public void handleStockReserved(StockReservedEvent event) {
        log.info("Received StockReserved event for order: {}", event.getOrderId());
        orderService.confirmOrder(event.getOrderId().toString());
    }

    @RabbitListener(queues = RabbitMQConfig.ORDERS_RESULTS_QUEUE)
    public void handleStockRejected(StockRejectedEvent event) {
        log.info("Received StockRejected event for order: {} - Reason: {}", 
                event.getOrderId(), event.getReason());
        orderService.cancelOrder(event.getOrderId().toString(), event.getReason());
    }
}
