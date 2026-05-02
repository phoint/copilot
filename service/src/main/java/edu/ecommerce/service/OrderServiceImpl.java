package edu.ecommerce.service;

import edu.ecommerce.core.dto.OrderItemRequest;
import edu.ecommerce.core.dto.OrderItemResponse;
import edu.ecommerce.core.dto.OrderRequest;
import edu.ecommerce.core.dto.OrderResponse;
import edu.ecommerce.core.dto.OrderUpdateRequest;
import edu.ecommerce.core.entity.Order;
import edu.ecommerce.core.entity.OrderItem;
import edu.ecommerce.core.entity.Product;
import edu.ecommerce.core.entity.User;
import edu.ecommerce.core.enums.OrderStatus;
import edu.ecommerce.core.exception.InvalidOrderTransitionException;
import edu.ecommerce.core.exception.OrderNotFoundException;
import edu.ecommerce.core.exception.ProductNotFoundException;
import edu.ecommerce.core.exception.UserNotFoundException;
import edu.ecommerce.core.pricing.OrderPricingModifier;
import edu.ecommerce.service.repository.OrderItemRepository;
import edu.ecommerce.service.repository.OrderRepository;
import edu.ecommerce.service.repository.UserRepository;
import edu.ecommerce.service.repository.ProductRepository;
import edu.ecommerce.service.statemachine.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final List<OrderPricingModifier> pricingModifiers;
    private final StateMachineFactory<OrderStatus, OrderEvent> stateMachineFactory;

    private static final Map<OrderStatus, OrderEvent> STATUS_TO_EVENT = Map.of(
        OrderStatus.CONFIRMED, OrderEvent.CONFIRM,
        OrderStatus.PROCESSING, OrderEvent.START_PROCESSING,
        OrderStatus.SHIPPED, OrderEvent.SHIP,
        OrderStatus.DELIVERED, OrderEvent.DELIVER,
        OrderStatus.CANCELLED, OrderEvent.CANCEL,
        OrderStatus.REFUNDED, OrderEvent.REFUND
    );

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        User user = findUserById(request.getUserId());

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress());
        order.setPromoCode(request.getPromoCode());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setShippingCost(BigDecimal.ZERO);
        order.setMembershipDiscount(BigDecimal.ZERO);
        order.setTotalAmount(BigDecimal.ZERO);

        orderRepository.save(order);

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = findProductById(itemRequest.getProductId());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(product.getPrice());

            order.getItems().add(item);
        }

        orderRepository.save(order);

        for (OrderPricingModifier modifier : pricingModifiers) {
            modifier.apply(order);
        }

        recalculateTotalAmount(order);
        orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = findOrderById(id);
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> listAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
            .map(this::mapToOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrdersByUser(Long userId, Pageable pageable) {
        findUserById(userId);
        return orderRepository.findByUserId(userId, pageable)
            .map(this::mapToOrderResponse);
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, OrderStatus nextStatus) {
        Order order = findOrderById(id);
        applyTransition(order, nextStatus);
        orderRepository.save(order);
        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse updateOrder(Long id, OrderUpdateRequest request) {
        Order order = findOrderById(id);

        if (request.getStatus() != null) {
            applyTransition(order, request.getStatus());
        }

        if (request.getShippingAddress() != null) {
            order.setShippingAddress(request.getShippingAddress());
        }

        if (request.getPromoCode() != null) {
            order.setPromoCode(request.getPromoCode());
        }

        orderRepository.save(order);
        return mapToOrderResponse(order);
    }

    @Override
    public void cancelOrder(Long id) {
        Order order = findOrderById(id);
        applyTransition(order, OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private void applyTransition(Order order, OrderStatus nextStatus) {
        OrderEvent event = STATUS_TO_EVENT.get(nextStatus);
        if (event == null) {
            throw new InvalidOrderTransitionException(
                "Cannot transition to status: " + nextStatus);
        }

        StateMachine<OrderStatus, OrderEvent> machine = stateMachineFactory.getStateMachine();
        machine.stopReactively().block();
        machine.getStateMachineAccessor().doWithAllRegions(access ->
            access.resetStateMachineReactively(
                new DefaultStateMachineContext<>(order.getStatus(), null, null, null)
            ).block()
        );
        machine.startReactively().block();

        StateMachineEventResult<OrderStatus, OrderEvent> result = machine
            .sendEvent(Mono.just(MessageBuilder.withPayload(event).setHeader("order", order).build()))
            .blockFirst();

        if (result == null || result.getResultType() == StateMachineEventResult.ResultType.DENIED) {
            throw new InvalidOrderTransitionException(
                String.format("Cannot transition from %s to %s", order.getStatus(), nextStatus));
        }

        order.setStatus(nextStatus);
    }

    private void recalculateTotalAmount(Order order) {
        BigDecimal subtotal = order.getItems().stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = subtotal
            .add(order.getShippingCost())
            .subtract(order.getDiscountAmount())
            .subtract(order.getMembershipDiscount());

        order.setTotalAmount(total.max(BigDecimal.ZERO));
    }

    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setUserName(order.getUser().getUsername());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setPromoCode(order.getPromoCode());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setShippingCost(order.getShippingCost());
        response.setMembershipDiscount(order.getMembershipDiscount());
        response.setTotalAmount(order.getTotalAmount());
        response.setItems(order.getItems().stream()
            .map(this::mapToOrderItemResponse)
            .toList());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(
            item.getId(),
            item.getProduct().getId(),
            item.getProduct().getName(),
            item.getQuantity(),
            item.getUnitPrice()
        );
    }
}
