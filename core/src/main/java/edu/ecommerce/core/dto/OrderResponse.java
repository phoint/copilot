package edu.ecommerce.core.dto;

import edu.ecommerce.core.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long userId;
    private String userName;
    private OrderStatus status;
    private String shippingAddress;
    private String promoCode;
    private BigDecimal discountAmount;
    private BigDecimal shippingCost;
    private BigDecimal membershipDiscount;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
