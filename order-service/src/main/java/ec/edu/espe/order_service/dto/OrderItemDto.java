package ec.edu.espe.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private UUID productId; // Input JSON string, parsing to UUID or keeping String. Model uses UUID or String?
                            // My model OrderItem uses UUID productId. So keeping UUID here is fine, Jackson handles it.
    private Integer quantity;
}
