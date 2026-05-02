package edu.ecommerce.service;

import edu.ecommerce.core.dto.OrderRequest;
import edu.ecommerce.core.dto.OrderResponse;
import edu.ecommerce.core.dto.OrderUpdateRequest;
import edu.ecommerce.core.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    Page<OrderResponse> listAllOrders(Pageable pageable);

    Page<OrderResponse> listOrdersByUser(Long userId, Pageable pageable);

    OrderResponse updateOrderStatus(Long id, OrderStatus nextStatus);

    OrderResponse updateOrder(Long id, OrderUpdateRequest request);

    void cancelOrder(Long id);
}
