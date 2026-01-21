package ec.edu.espe.order_service.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderCreatedEvent {
    private String eventType;
    private String orderId;
    private String correlationId;
    private LocalDateTime createdAt;
    private List<OrderItemDto> items;
}
