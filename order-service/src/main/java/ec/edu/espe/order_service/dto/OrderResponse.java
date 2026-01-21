package ec.edu.espe.order_service.dto;

import ec.edu.espe.order_service.model.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String orderId;
    private String customerId;
    private OrderStatus status;
    private String message;
    private String reason;
    private List<OrderItemDto> items;
    private LocalDateTime updatedAt;
}
