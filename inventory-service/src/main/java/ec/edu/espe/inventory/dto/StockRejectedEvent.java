package ec.edu.espe.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRejectedEvent {
    private UUID orderId;
    private String status = "REJECTED";
    private String reason;
    private LocalDateTime timestamp;

    public StockRejectedEvent(UUID orderId, String reason) {
        this.orderId = orderId;
        this.status = "REJECTED";
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }
}
