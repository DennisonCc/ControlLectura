package ec.edu.espe.order_service.dto;

import lombok.Data;

@Data
public class ShippingAddressDto {
    private String country;
    private String city;
    private String street;
    private String postalCode;
    private String zip;
}
