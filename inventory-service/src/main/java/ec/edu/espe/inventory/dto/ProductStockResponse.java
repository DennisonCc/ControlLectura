package ec.edu.espe.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockResponse {
    private UUID productId;
    private Integer availableStock;
    private Integer reservedStock;
    private LocalDateTime updatedAt;
}
