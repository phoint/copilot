package edu.ecommerce.api.repository;

import edu.ecommerce.core.entity.Order;
import edu.ecommerce.core.entity.User;
import edu.ecommerce.core.enums.OrderStatus;
import edu.ecommerce.service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class OrderRepositoryJpaTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User user1;
    private User user2;
    private Order order1;
    private Order order2;
    private Order order3;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("hashed");
        user1.setRole("USER");
        user1.setStatus("ACTIVE");

        user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("hashed");
        user2.setRole("USER");
        user2.setStatus("ACTIVE");

        order1 = new Order();
        order1.setUser(user1);
        order1.setStatus(OrderStatus.PENDING);
        order1.setShippingAddress("123 Main St");
        order1.setTotalAmount(new BigDecimal("100.00"));
        order1.setDiscountAmount(BigDecimal.ZERO);
        order1.setShippingCost(new BigDecimal("10.00"));
        order1.setMembershipDiscount(BigDecimal.ZERO);

        order2 = new Order();
        order2.setUser(user1);
        order2.setStatus(OrderStatus.CONFIRMED);
        order2.setShippingAddress("123 Main St");
        order2.setTotalAmount(new BigDecimal("200.00"));
        order2.setDiscountAmount(BigDecimal.ZERO);
        order2.setShippingCost(new BigDecimal("15.00"));
        order2.setMembershipDiscount(BigDecimal.ZERO);

        order3 = new Order();
        order3.setUser(user2);
        order3.setStatus(OrderStatus.PROCESSING);
        order3.setShippingAddress("456 Oak Ave");
        order3.setTotalAmount(new BigDecimal("300.00"));
        order3.setDiscountAmount(BigDecimal.ZERO);
        order3.setShippingCost(new BigDecimal("20.00"));
        order3.setMembershipDiscount(BigDecimal.ZERO);
    }

    @Test
    void testSaveOrder_ShouldPersistAndGenerateId() {
        testEntityManager.persistAndFlush(user1);
        Order saved = orderRepository.save(order1);
        testEntityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isGreaterThan(0);
    }

    @Test
    void testFindByUserId_ShouldReturnOrdersForUser() {
        User savedUser1 = testEntityManager.persistAndFlush(user1);
        User savedUser2 = testEntityManager.persistAndFlush(user2);

        order1.setUser(savedUser1);
        order2.setUser(savedUser1);
        order3.setUser(savedUser2);

        testEntityManager.persistAndFlush(order1);
        testEntityManager.persistAndFlush(order2);
        testEntityManager.persistAndFlush(order3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> userOrders = orderRepository.findByUserId(savedUser1.getId(), pageable);

        assertThat(userOrders.getContent())
            .hasSize(2)
            .allMatch(order -> order.getUser().getId().equals(savedUser1.getId()));
    }

    @Test
    void testFindByUserId_ShouldReturnEmptyWhenUserHasNoOrders() {
        User savedUser = testEntityManager.persistAndFlush(user1);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> userOrders = orderRepository.findByUserId(savedUser.getId(), pageable);

        assertThat(userOrders.getContent()).isEmpty();
    }

    @Test
    void testFindByUserId_Pagination_ShouldReturnCorrectPage() {
        User savedUser = testEntityManager.persistAndFlush(user1);

        Order o1 = new Order();
        o1.setUser(savedUser);
        o1.setStatus(OrderStatus.PENDING);
        o1.setShippingAddress("Addr1");
        o1.setTotalAmount(new BigDecimal("100.00"));
        o1.setDiscountAmount(BigDecimal.ZERO);
        o1.setShippingCost(BigDecimal.ZERO);
        o1.setMembershipDiscount(BigDecimal.ZERO);

        Order o2 = new Order();
        o2.setUser(savedUser);
        o2.setStatus(OrderStatus.CONFIRMED);
        o2.setShippingAddress("Addr2");
        o2.setTotalAmount(new BigDecimal("200.00"));
        o2.setDiscountAmount(BigDecimal.ZERO);
        o2.setShippingCost(BigDecimal.ZERO);
        o2.setMembershipDiscount(BigDecimal.ZERO);

        Order o3 = new Order();
        o3.setUser(savedUser);
        o3.setStatus(OrderStatus.PROCESSING);
        o3.setShippingAddress("Addr3");
        o3.setTotalAmount(new BigDecimal("300.00"));
        o3.setDiscountAmount(BigDecimal.ZERO);
        o3.setShippingCost(BigDecimal.ZERO);
        o3.setMembershipDiscount(BigDecimal.ZERO);

        testEntityManager.persistAndFlush(o1);
        testEntityManager.persistAndFlush(o2);
        testEntityManager.persistAndFlush(o3);

        Pageable pageOne = PageRequest.of(0, 2);
        Page<Order> pageOneResult = orderRepository.findByUserId(savedUser.getId(), pageOne);

        assertThat(pageOneResult.getContent()).hasSize(2);
        assertThat(pageOneResult.getTotalElements()).isGreaterThanOrEqualTo(3L);
        assertThat(pageOneResult.hasNext()).isTrue();
    }

    @Test
    void testFindByStatus_ShouldReturnOrdersByStatus() {
        User savedUser1 = testEntityManager.persistAndFlush(user1);
        User savedUser2 = testEntityManager.persistAndFlush(user2);

        order1.setUser(savedUser1);
        order1.setStatus(OrderStatus.PENDING);
        order2.setUser(savedUser1);
        order2.setStatus(OrderStatus.PENDING);
        order3.setUser(savedUser2);
        order3.setStatus(OrderStatus.PROCESSING);

        testEntityManager.persistAndFlush(order1);
        testEntityManager.persistAndFlush(order2);
        testEntityManager.persistAndFlush(order3);

        Pageable pageable = PageRequest.of(0, 100);
        Page<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING, pageable);

        assertThat(pendingOrders.getContent())
            .hasSizeGreaterThanOrEqualTo(2)
            .allMatch(order -> order.getStatus() == OrderStatus.PENDING);
    }

    @Test
    void testFindByStatus_ShouldReturnOnlyMatchingStatus() {
        User savedUser = testEntityManager.persistAndFlush(user1);

        order1.setUser(savedUser);
        order1.setStatus(OrderStatus.PENDING);
        testEntityManager.persistAndFlush(order1);

        Pageable pageable = PageRequest.of(0, 100);
        Page<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING, pageable);

        assertThat(pendingOrders.getContent())
            .hasSizeGreaterThanOrEqualTo(1)
            .allMatch(o -> o.getStatus() == OrderStatus.PENDING);
    }

    @Test
    void testFindByStatus_Pagination_ShouldReturnCorrectPage() {
        User savedUser = testEntityManager.persistAndFlush(user1);

        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setUser(savedUser);
            order.setStatus(OrderStatus.CONFIRMED);
            order.setShippingAddress("Address " + i);
            order.setTotalAmount(new BigDecimal("100.00"));
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setShippingCost(BigDecimal.ZERO);
            order.setMembershipDiscount(BigDecimal.ZERO);
            testEntityManager.persistAndFlush(order);
        }

        Pageable pageOne = PageRequest.of(0, 3);
        Page<Order> pageOneResult = orderRepository.findByStatus(OrderStatus.CONFIRMED, pageOne);

        assertThat(pageOneResult.getContent()).hasSize(3);
        assertThat(pageOneResult.getTotalElements()).isGreaterThanOrEqualTo(5L);
        assertThat(pageOneResult.hasNext()).isTrue();
    }

    @Test
    void testFindById_ShouldReturnOrder() {
        User savedUser = testEntityManager.persistAndFlush(user1);
        order1.setUser(savedUser);
        Order saved = testEntityManager.persistAndFlush(order1);

        Optional<Order> found = orderRepository.findById(saved.getId());

        assertThat(found)
            .isPresent()
            .hasValueSatisfying(order -> assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING));
    }

    @Test
    void testFindAll_ShouldReturnAllOrders() {
        User savedUser1 = testEntityManager.persistAndFlush(user1);
        User savedUser2 = testEntityManager.persistAndFlush(user2);

        order1.setUser(savedUser1);
        order2.setUser(savedUser1);
        order3.setUser(savedUser2);

        testEntityManager.persistAndFlush(order1);
        testEntityManager.persistAndFlush(order2);
        testEntityManager.persistAndFlush(order3);

        assertThat(orderRepository.findAll())
            .hasSizeGreaterThanOrEqualTo(3)
            .anyMatch(o -> o.getId().equals(order1.getId()))
            .anyMatch(o -> o.getId().equals(order2.getId()))
            .anyMatch(o -> o.getId().equals(order3.getId()));
    }

    @Test
    void testCount_ShouldReturnTotalOrders() {
        User savedUser1 = testEntityManager.persistAndFlush(user1);
        User savedUser2 = testEntityManager.persistAndFlush(user2);

        order1.setUser(savedUser1);
        order2.setUser(savedUser1);
        order3.setUser(savedUser2);

        testEntityManager.persistAndFlush(order1);
        testEntityManager.persistAndFlush(order2);
        testEntityManager.persistAndFlush(order3);

        long count = orderRepository.count();

        assertThat(count).isGreaterThanOrEqualTo(3L);
    }

    @Test
    void testUpdate_ShouldModifyExistingOrder() {
        User savedUser = testEntityManager.persistAndFlush(user1);
        order1.setUser(savedUser);
        Order saved = testEntityManager.persistAndFlush(order1);

        saved.setStatus(OrderStatus.SHIPPED);
        saved.setShippingAddress("Updated Address");
        orderRepository.save(saved);
        testEntityManager.flush();

        Order updated = testEntityManager.find(Order.class, saved.getId());

        assertThat(updated.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(updated.getShippingAddress()).isEqualTo("Updated Address");
    }

    @Test
    void testDelete_ShouldRemoveOrder() {
        User savedUser = testEntityManager.persistAndFlush(user1);
        order1.setUser(savedUser);
        Order saved = testEntityManager.persistAndFlush(order1);
        Long orderId = saved.getId();

        orderRepository.deleteById(orderId);
        testEntityManager.flush();

        Order deleted = testEntityManager.find(Order.class, orderId);

        assertThat(deleted).isNull();
    }
}
