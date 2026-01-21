package ec.edu.espe.order_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class StockRejectedEvent {
    private String eventType;
    private String orderId;
    private String correlationId;
    private String reason;
    private LocalDateTime rejectedAt;
}
