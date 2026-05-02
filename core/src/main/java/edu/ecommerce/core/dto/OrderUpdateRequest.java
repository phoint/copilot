package edu.ecommerce.core.dto;

import edu.ecommerce.core.enums.OrderStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateRequest {
    private OrderStatus status;

    @Size(max = 255)
    private String shippingAddress;

    @Size(max = 50)
    private String promoCode;
}
