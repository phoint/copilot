package edu.ecommerce.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotNull
    private Long userId;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;

    @Size(max = 255)
    private String shippingAddress;

    @Size(max = 50)
    private String promoCode;
}
