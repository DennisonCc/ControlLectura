package ec.edu.espe.order_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String customerId;
    private List<OrderItemDto> items;
    private ShippingAddressDto shippingAddress;
    private String paymentReference;
}
