package edu.ecommerce.api.statemachine;

import edu.ecommerce.core.entity.Order;
import edu.ecommerce.core.entity.OrderItem;
import edu.ecommerce.core.enums.OrderStatus;
import edu.ecommerce.service.statemachine.OrderEvent;
import edu.ecommerce.service.statemachine.guards.OrderGuards;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class OrderGuardsTest {

    @Autowired
    private OrderGuards orderGuards;

    private StateContext<OrderStatus, OrderEvent> mockContext;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        mockContext = mock(StateContext.class);
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setItems(new ArrayList<>());
        testOrder.setTotalAmount(BigDecimal.ZERO);
        testOrder.setDiscountAmount(BigDecimal.ZERO);
        testOrder.setShippingCost(BigDecimal.ZERO);
        testOrder.setMembershipDiscount(BigDecimal.ZERO);
    }

    // =====================================================================
    // hasItems() Guard Tests
    // =====================================================================

    @Test
    void testHasItems_EmptyItems_ReturnsFalse() {
        testOrder.setItems(new ArrayList<>());
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.hasItems();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isFalse();
    }

    @Test
    void testHasItems_WithItems_ReturnsTrue() {
        OrderItem item = new OrderItem();
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("100.00"));
        testOrder.setItems(List.of(item));
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.hasItems();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    @Test
    void testHasItems_MultipleItems_ReturnsTrue() {
        OrderItem item1 = new OrderItem();
        item1.setQuantity(1);
        OrderItem item2 = new OrderItem();
        item2.setQuantity(2);
        testOrder.setItems(List.of(item1, item2));
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.hasItems();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    // =====================================================================
    // hasValidTotal() Guard Tests
    // =====================================================================

    @Test
    void testHasValidTotal_NegativeTotal_ReturnsFalse() {
        testOrder.setTotalAmount(new BigDecimal("-100.00"));
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.hasValidTotal();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isFalse();
    }

    @Test
    void testHasValidTotal_ZeroTotal_ReturnsTrue() {
        testOrder.setTotalAmount(BigDecimal.ZERO);
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.hasValidTotal();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    @Test
    void testHasValidTotal_PositiveTotal_ReturnsTrue() {
        testOrder.setTotalAmount(new BigDecimal("100.00"));
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.hasValidTotal();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    @Test
    void testHasValidTotal_LargeTotal_ReturnsTrue() {
        testOrder.setTotalAmount(new BigDecimal("999999.99"));
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.hasValidTotal();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    // =====================================================================
    // hasShippingAddress() Guard Tests
    // =====================================================================

    @Test
    void testHasShippingAddress_Null_ReturnsFalse() {
        testOrder.setShippingAddress(null);
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.hasShippingAddress();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isFalse();
    }

    @Test
    void testHasShippingAddress_EmptyString_ReturnsFalse() {
        testOrder.setShippingAddress("");
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.hasShippingAddress();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isFalse();
    }

    @Test
    void testHasShippingAddress_BlankString_ReturnsFalse() {
        testOrder.setShippingAddress("   ");
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.hasShippingAddress();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isFalse();
    }

    @Test
    void testHasShippingAddress_ValidAddress_ReturnsTrue() {
        testOrder.setShippingAddress("123 Main Street, City, State 12345");
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.hasShippingAddress();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    @Test
    void testHasShippingAddress_SimpleAddress_ReturnsTrue() {
        testOrder.setShippingAddress("123 Main St");
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.hasShippingAddress();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    // =====================================================================
    // canBeCancelled() Guard Tests
    // =====================================================================

    @Test
    void testCanBeCancelled_FromPending_ReturnsTrue() {
        testOrder.setStatus(OrderStatus.PENDING);
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.canBeCancelled();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    @Test
    void testCanBeCancelled_FromConfirmed_ReturnsTrue() {
        testOrder.setStatus(OrderStatus.CONFIRMED);
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.canBeCancelled();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    @Test
    void testCanBeCancelled_FromProcessing_ReturnsTrue() {
        testOrder.setStatus(OrderStatus.PROCESSING);
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.canBeCancelled();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    @Test
    void testCanBeCancelled_FromShipped_ReturnsFalse() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.canBeCancelled();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isFalse();
    }

    @Test
    void testCanBeCancelled_FromDelivered_ReturnsFalse() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.canBeCancelled();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isFalse();
    }

    @Test
    void testCanBeCancelled_FromCancelled_ReturnsFalse() {
        testOrder.setStatus(OrderStatus.CANCELLED);
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.canBeCancelled();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isFalse();
    }

    @Test
    void testCanBeCancelled_FromRefunded_ReturnsFalse() {
        testOrder.setStatus(OrderStatus.REFUNDED);
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.canBeCancelled();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isFalse();
    }

    // =====================================================================
    // withinMaxOrderValue() Guard Tests
    // =====================================================================

    @Test
    void testWithinMaxOrderValue_BelowLimit_ReturnsTrue() {
        testOrder.setTotalAmount(new BigDecimal("50000.00"));
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.withinMaxOrderValue();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    @Test
    void testWithinMaxOrderValue_AtLimit_ReturnsTrue() {
        testOrder.setTotalAmount(new BigDecimal("100000.00"));
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.withinMaxOrderValue();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    @Test
    void testWithinMaxOrderValue_AboveLimit_ReturnsFalse() {
        testOrder.setTotalAmount(new BigDecimal("100000.01"));
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.withinMaxOrderValue();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isFalse();
    }

    @Test
    void testWithinMaxOrderValue_WayAboveLimit_ReturnsFalse() {
        testOrder.setTotalAmount(new BigDecimal("999999.99"));
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.withinMaxOrderValue();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isFalse();
    }

    @Test
    void testWithinMaxOrderValue_Zero_ReturnsTrue() {
        testOrder.setTotalAmount(BigDecimal.ZERO);
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.withinMaxOrderValue();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    // =====================================================================
    // notAlreadyRefunded() Guard Tests
    // =====================================================================

    @Test
    void testNotAlreadyRefunded_FromNonRefundedState_ReturnsTrue() {
        testOrder.setStatus(OrderStatus.PENDING);
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.notAlreadyRefunded();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    @Test
    void testNotAlreadyRefunded_FromDeliveredState_ReturnsTrue() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.notAlreadyRefunded();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isTrue();
    }

    @Test
    void testNotAlreadyRefunded_FromRefundedState_ReturnsFalse() {
        testOrder.setStatus(OrderStatus.REFUNDED);
        setupMockContext(testOrder);

        Guard<OrderStatus, OrderEvent> guard = orderGuards.notAlreadyRefunded();
        boolean result = guard.evaluate(mockContext);

        assertThat(result).isFalse();
    }

    // =====================================================================
    // Combined Guard Tests (Multiple Guards for Single Transition)
    // =====================================================================

    @Test
    void testGuards_CreateOrder_AllConditionsMet() {
        OrderItem item = new OrderItem();
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("100.00"));
        testOrder.setItems(List.of(item));
        testOrder.setTotalAmount(new BigDecimal("100.00"));
        testOrder.setShippingAddress("123 Main St");
        setupMockContext(testOrder);

        // Test hasItems
        assertThat(orderGuards.hasItems().evaluate(mockContext)).isTrue();
        // Test hasValidTotal
        assertThat(orderGuards.hasValidTotal().evaluate(mockContext)).isTrue();
        // Test withinMaxOrderValue
        assertThat(orderGuards.withinMaxOrderValue().evaluate(mockContext)).isTrue();
    }

    @Test
    void testGuards_CreateOrder_MissingItems() {
        testOrder.setItems(new ArrayList<>());
        testOrder.setTotalAmount(BigDecimal.ZERO);
        testOrder.setShippingAddress("123 Main St");
        setupMockContext(testOrder);

        // Test hasItems - should fail
        assertThat(orderGuards.hasItems().evaluate(mockContext)).isFalse();
        // Other guards should pass
        assertThat(orderGuards.hasValidTotal().evaluate(mockContext)).isTrue();
        assertThat(orderGuards.withinMaxOrderValue().evaluate(mockContext)).isTrue();
    }

    @Test
    void testGuards_CreateOrder_ExceedsMaxValue() {
        OrderItem item = new OrderItem();
        item.setQuantity(1);
        testOrder.setItems(List.of(item));
        testOrder.setTotalAmount(new BigDecimal("150000.00"));
        testOrder.setShippingAddress("123 Main St");
        setupMockContext(testOrder);

        // Test withinMaxOrderValue - should fail
        assertThat(orderGuards.withinMaxOrderValue().evaluate(mockContext)).isFalse();
        // Other guards should pass
        assertThat(orderGuards.hasItems().evaluate(mockContext)).isTrue();
        assertThat(orderGuards.hasValidTotal().evaluate(mockContext)).isTrue();
    }

    @Test
    void testGuards_ShipOrder_RequiresShippingAddress() {
        OrderItem item = new OrderItem();
        item.setQuantity(1);
        testOrder.setItems(List.of(item));
        testOrder.setTotalAmount(new BigDecimal("100.00"));
        testOrder.setShippingAddress(null);
        testOrder.setStatus(OrderStatus.PROCESSING);
        setupMockContext(testOrder);

        // Test hasShippingAddress - should fail
        assertThat(orderGuards.hasShippingAddress().evaluate(mockContext)).isFalse();
    }

    @Test
    void testGuards_CancelOrder_CannotCancelFromTerminalState() {
        testOrder.setStatus(OrderStatus.REFUNDED);
        setupMockContext(testOrder);

        // Test canBeCancelled - should fail for terminal state
        assertThat(orderGuards.canBeCancelled().evaluate(mockContext)).isFalse();
    }

    @Test
    void testGuards_RefundOrder_IdempotencyCheck() {
        testOrder.setStatus(OrderStatus.REFUNDED);
        setupMockContext(testOrder);

        // Test notAlreadyRefunded - should fail (already refunded)
        assertThat(orderGuards.notAlreadyRefunded().evaluate(mockContext)).isFalse();
    }

    private void setupMockContext(Order order) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("order", order);
        MessageHeaders headers = new MessageHeaders(headerMap);
        when(mockContext.getMessageHeaders()).thenReturn(headers);
    }
}
