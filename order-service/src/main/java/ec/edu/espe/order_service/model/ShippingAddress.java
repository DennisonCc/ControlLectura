package ec.edu.espe.order_service.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
    private String country;
    private String city;
    private String street;
    private String postalCode;
    private String zip; // Requirements use zip in input example, postalCode in first example. I'll support both or map one.
}
