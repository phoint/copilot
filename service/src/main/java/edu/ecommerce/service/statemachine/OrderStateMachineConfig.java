package edu.ecommerce.service.statemachine;

import edu.ecommerce.core.enums.OrderStatus;
import edu.ecommerce.service.statemachine.guards.OrderGuards;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

import static edu.ecommerce.core.enums.OrderStatus.*;
import static edu.ecommerce.service.statemachine.OrderEvent.*;

@Configuration
@EnableStateMachineFactory
public class OrderStateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderStatus, OrderEvent> {

    private final OrderGuards orderGuards;

    public OrderStateMachineConfig(OrderGuards orderGuards) {
        this.orderGuards = orderGuards;
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderStatus, OrderEvent> config)
            throws Exception {
        config.withConfiguration().autoStartup(false);
    }

    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEvent> states)
            throws Exception {
        states.withStates()
            .initial(PENDING)
            .states(EnumSet.allOf(OrderStatus.class))
            .end(CANCELLED)
            .end(REFUNDED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEvent> transitions)
            throws Exception {
        transitions
            .withExternal()
                .source(PENDING).target(CONFIRMED).event(CONFIRM)
                .guard(orderGuards.hasItems())
                .guard(orderGuards.withinMaxOrderValue())
                .and()
            .withExternal()
                .source(CONFIRMED).target(PROCESSING).event(START_PROCESSING)
                .guard(orderGuards.hasValidTotal())
                .and()
            .withExternal()
                .source(PROCESSING).target(SHIPPED).event(SHIP)
                .guard(orderGuards.hasShippingAddress())
                .and()
            .withExternal()
                .source(SHIPPED).target(DELIVERED).event(DELIVER)
                .and()
            .withExternal()
                .source(SHIPPED).target(REFUNDED).event(REFUND)
                .and()
            .withExternal()
                .source(DELIVERED).target(REFUNDED).event(REFUND)
                .and()
            .withExternal()
                .source(PENDING).target(CANCELLED).event(CANCEL)
                .guard(orderGuards.canBeCancelled())
                .and()
            .withExternal()
                .source(CONFIRMED).target(CANCELLED).event(CANCEL)
                .guard(orderGuards.canBeCancelled())
                .and()
            .withExternal()
                .source(PROCESSING).target(CANCELLED).event(CANCEL)
                .guard(orderGuards.canBeCancelled());
    }
}
