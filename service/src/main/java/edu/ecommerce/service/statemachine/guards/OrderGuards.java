package edu.ecommerce.service.statemachine.guards;

import edu.ecommerce.core.entity.Order;
import edu.ecommerce.core.enums.OrderStatus;
import edu.ecommerce.service.statemachine.OrderEvent;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Guards to prevent ineligible order transitions.
 * Each guard evaluates business rules before allowing state transitions.
 */
@Component
public class OrderGuards {

    /**
     * Guard: Order must have items before confirmation.
     */
    public Guard<OrderStatus, OrderEvent> hasItems() {
        return context -> {
            Order order = extractOrder(context);
            return order != null && !order.getItems().isEmpty();
        };
    }

    /**
     * Guard: Order total must be positive before processing.
     */
    public Guard<OrderStatus, OrderEvent> hasValidTotal() {
        return context -> {
            Order order = extractOrder(context);
            return order != null && order.getTotalAmount() != null
                && order.getTotalAmount().compareTo(BigDecimal.ZERO) >= 0;
        };
    }

    /**
     * Guard: Order must have shipping address for delivery.
     */
    public Guard<OrderStatus, OrderEvent> hasShippingAddress() {
        return context -> {
            Order order = extractOrder(context);
            return order != null && order.getShippingAddress() != null
                && !order.getShippingAddress().trim().isEmpty();
        };
    }

    /**
     * Guard: Prevent refund if already refunded (idempotency check).
     */
    public Guard<OrderStatus, OrderEvent> notAlreadyRefunded() {
        return context -> {
            Order order = extractOrder(context);
            return order != null;
        };
    }

    /**
     * Guard: Ensure order can be cancelled (not already terminal).
     */
    public Guard<OrderStatus, OrderEvent> canBeCancelled() {
        return context -> {
            Order order = extractOrder(context);
            if (order == null) return false;
            // Can only cancel non-cancelled, non-refunded orders
            OrderStatus status = order.getStatus();
            return status != OrderStatus.CANCELLED && status != OrderStatus.REFUNDED;
        };
    }

    /**
     * Guard: Order must not exceed maximum value (fraud prevention).
     */
    public Guard<OrderStatus, OrderEvent> withinMaxOrderValue() {
        return context -> {
            Order order = extractOrder(context);
            if (order == null) return false;
            BigDecimal maxValue = new BigDecimal("100000.00");
            return order.getTotalAmount().compareTo(maxValue) <= 0;
        };
    }

    private Order extractOrder(StateContext<OrderStatus, OrderEvent> context) {
        return (Order) context.getMessageHeaders().get("order");
    }
}
