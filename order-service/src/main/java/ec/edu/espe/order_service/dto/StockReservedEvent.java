package ec.edu.espe.order_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class StockReservedEvent {
    private String eventType;
    private String orderId;
    private String correlationId;
    private List<OrderItemDto> reservedItems;
    private LocalDateTime reservedAt;
}
