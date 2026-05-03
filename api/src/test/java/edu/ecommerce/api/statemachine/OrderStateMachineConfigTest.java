package edu.ecommerce.api.statemachine;

import edu.ecommerce.core.entity.Order;
import edu.ecommerce.core.entity.OrderItem;
import edu.ecommerce.core.enums.OrderStatus;
import edu.ecommerce.service.statemachine.OrderEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderStateMachineConfigTest {

    @Autowired
    private StateMachineFactory<OrderStatus, OrderEvent> stateMachineFactory;

    private StateMachine<OrderStatus, OrderEvent> stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = stateMachineFactory.getStateMachine();
    }

    @Test
    void testInitialState_IsPending() {
        Order order = createOrderWithStatus(OrderStatus.PENDING);
        setupStateWithOrder(order);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void testTransition_PendingToConfirmed() {
        Order order = createOrderWithItems(OrderStatus.PENDING, 1);
        order.setTotalAmount(new BigDecimal("50000.00"));
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.CONFIRM);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void testTransition_ConfirmedToProcessing() {
        Order order = createOrderWithItems(OrderStatus.CONFIRMED, 1);
        order.setTotalAmount(new BigDecimal("100.00"));
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.START_PROCESSING);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void testTransition_ProcessingToShipped() {
        Order order = createOrderWithItems(OrderStatus.PROCESSING, 1);
        order.setShippingAddress("123 Main St");
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.SHIP);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void testTransition_ShippedToDelivered() {
        Order order = createOrderWithStatus(OrderStatus.SHIPPED);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.DELIVER);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void testTransition_DeliveredToRefunded() {
        Order order = createOrderWithStatus(OrderStatus.DELIVERED);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.REFUND);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    void testTransition_ShippedToRefunded() {
        Order order = createOrderWithStatus(OrderStatus.SHIPPED);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.REFUND);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    void testTransition_PendingToCancelled() {
        Order order = createOrderWithStatus(OrderStatus.PENDING);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.CANCEL);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void testTransition_ConfirmedToCancelled() {
        Order order = createOrderWithStatus(OrderStatus.CONFIRMED);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.CANCEL);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void testTransition_ProcessingToCancelled() {
        Order order = createOrderWithStatus(OrderStatus.PROCESSING);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.CANCEL);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void testTerminalState_CancelledIsTerminal() {
        Order order = createOrderWithStatus(OrderStatus.CANCELLED);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.CONFIRM);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void testTerminalState_RefundedIsTerminal() {
        Order order = createOrderWithStatus(OrderStatus.REFUNDED);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.CANCEL);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    void testInvalidTransition_PendingToShipped_Denied() {
        Order order = createOrderWithStatus(OrderStatus.PENDING);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.SHIP);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void testInvalidTransition_ConfirmedToShipped_Denied() {
        Order order = createOrderWithStatus(OrderStatus.CONFIRMED);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.SHIP);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void testInvalidTransition_PendingToDelivered_Denied() {
        Order order = createOrderWithStatus(OrderStatus.PENDING);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.DELIVER);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void testFullLifecycle_PendingToRefunded() {
        Order order = createOrderWithItems(OrderStatus.PENDING, 1);
        order.setTotalAmount(new BigDecimal("50000.00"));
        order.setShippingAddress("123 Main St");
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.CONFIRM);
        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.CONFIRMED);

        order.setStatus(OrderStatus.CONFIRMED);
        sendEvent(order, OrderEvent.START_PROCESSING);
        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.PROCESSING);

        order.setStatus(OrderStatus.PROCESSING);
        sendEvent(order, OrderEvent.SHIP);
        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.SHIPPED);

        order.setStatus(OrderStatus.SHIPPED);
        sendEvent(order, OrderEvent.DELIVER);
        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.DELIVERED);

        order.setStatus(OrderStatus.DELIVERED);
        sendEvent(order, OrderEvent.REFUND);
        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    void testFullLifecycle_PendingToCancelled() {
        Order order = createOrderWithItems(OrderStatus.PENDING, 1);
        order.setTotalAmount(new BigDecimal("50000.00"));
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.CONFIRM);
        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.CONFIRMED);

        order.setStatus(OrderStatus.CONFIRMED);
        sendEvent(order, OrderEvent.START_PROCESSING);
        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.PROCESSING);

        order.setStatus(OrderStatus.PROCESSING);
        sendEvent(order, OrderEvent.CANCEL);
        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void testStateCount_AllSevenStatesExist() {
        assertThat(stateMachine.getStates()).hasSize(7);
        assertThat(stateMachine.getStates().stream().map(s -> s.getId()).toList())
            .contains(
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED,
                OrderStatus.PROCESSING,
                OrderStatus.SHIPPED,
                OrderStatus.DELIVERED,
                OrderStatus.CANCELLED,
                OrderStatus.REFUNDED
            );
    }

    @Test
    void testCanRefundFromShipped() {
        Order order = createOrderWithStatus(OrderStatus.SHIPPED);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.REFUND);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    void testCanCancelUntilProcessing() {
        Order order = createOrderWithStatus(OrderStatus.PENDING);
        setupStateWithOrder(order);
        sendEvent(order, OrderEvent.CANCEL);
        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.CANCELLED);

        stateMachine = stateMachineFactory.getStateMachine();
        order = createOrderWithStatus(OrderStatus.CONFIRMED);
        setupStateWithOrder(order);
        sendEvent(order, OrderEvent.CANCEL);
        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.CANCELLED);

        stateMachine = stateMachineFactory.getStateMachine();
        order = createOrderWithStatus(OrderStatus.PROCESSING);
        setupStateWithOrder(order);
        sendEvent(order, OrderEvent.CANCEL);
        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void testCannotCancelFromShipped() {
        Order order = createOrderWithStatus(OrderStatus.SHIPPED);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.CANCEL);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void testCannotCancelFromDelivered() {
        Order order = createOrderWithStatus(OrderStatus.DELIVERED);
        setupStateWithOrder(order);

        sendEvent(order, OrderEvent.CANCEL);

        assertThat(stateMachine.getState().getId()).isEqualTo(OrderStatus.DELIVERED);
    }

    private void setupStateWithOrder(Order order) {
        stateMachine.stopReactively().block();
        stateMachine.getStateMachineAccessor().doWithAllRegions(access ->
            access.resetStateMachineReactively(
                new org.springframework.statemachine.support.DefaultStateMachineContext<>(
                    order.getStatus(), null, null, null)
            ).block()
        );
        stateMachine.startReactively().block();
    }

    private void sendEvent(Order order, OrderEvent event) {
        stateMachine.sendEvent(Mono.just(MessageBuilder
            .withPayload(event)
            .setHeader("order", order)
            .build())).blockFirst();
    }

    private Order createOrderWithStatus(OrderStatus status) {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(status);
        order.setItems(new ArrayList<>());
        order.setTotalAmount(BigDecimal.ZERO);
        return order;
    }

    private Order createOrderWithItems(OrderStatus status, int itemCount) {
        Order order = createOrderWithStatus(status);
        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            OrderItem item = new OrderItem();
            item.setQuantity(1);
            item.setUnitPrice(new BigDecimal("100.00"));
            items.add(item);
        }
        order.setItems(items);
        return order;
    }
}
