package ec.edu.espe.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservedEvent {
    private UUID orderId;
    private String status = "RESERVED";
    private LocalDateTime timestamp;

    public StockReservedEvent(UUID orderId) {
        this.orderId = orderId;
        this.status = "RESERVED";
        this.timestamp = LocalDateTime.now();
    }
}
