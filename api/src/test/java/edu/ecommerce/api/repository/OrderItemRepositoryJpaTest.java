package edu.ecommerce.api.repository;

import edu.ecommerce.core.entity.Category;
import edu.ecommerce.core.entity.Order;
import edu.ecommerce.core.entity.OrderItem;
import edu.ecommerce.core.entity.Product;
import edu.ecommerce.core.entity.User;
import edu.ecommerce.core.enums.OrderStatus;
import edu.ecommerce.service.repository.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class OrderItemRepositoryJpaTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User user;
    private Order order;
    private Category category;
    private Product product1;
    private Product product2;
    private OrderItem orderItem1;
    private OrderItem orderItem2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("hashed");
        user.setRole("USER");
        user.setStatus("ACTIVE");

        order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress("123 Main St");
        order.setTotalAmount(new BigDecimal("500.00"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setShippingCost(new BigDecimal("10.00"));
        order.setMembershipDiscount(BigDecimal.ZERO);

        category = new Category();
        category.setName("Electronics");

        product1 = new Product();
        product1.setName("Laptop");
        product1.setDescription("High performance laptop");
        product1.setPrice(new BigDecimal("999.99"));
        product1.setStockQuantity(10);
        product1.setCategory(category);

        product2 = new Product();
        product2.setName("Mouse");
        product2.setDescription("Wireless mouse");
        product2.setPrice(new BigDecimal("29.99"));
        product2.setStockQuantity(50);
        product2.setCategory(category);

        orderItem1 = new OrderItem();
        orderItem1.setOrder(order);
        orderItem1.setProduct(product1);
        orderItem1.setQuantity(1);
        orderItem1.setUnitPrice(new BigDecimal("999.99"));

        orderItem2 = new OrderItem();
        orderItem2.setOrder(order);
        orderItem2.setProduct(product2);
        orderItem2.setQuantity(2);
        orderItem2.setUnitPrice(new BigDecimal("29.99"));
    }

    @Test
    void testSaveOrderItem_ShouldPersistAndGenerateId() {
        User savedUser = testEntityManager.persistAndFlush(user);
        order.setUser(savedUser);
        Order savedOrder = testEntityManager.persistAndFlush(order);

        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);

        orderItem1.setOrder(savedOrder);
        orderItem1.setProduct(product1);

        OrderItem saved = orderItemRepository.save(orderItem1);
        testEntityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isGreaterThan(0);
    }

    @Test
    void testFindByOrderId_ShouldReturnOrderItems() {
        User savedUser = testEntityManager.persistAndFlush(user);
        order.setUser(savedUser);
        Order savedOrder = testEntityManager.persistAndFlush(order);

        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);

        orderItem1.setOrder(savedOrder);
        orderItem1.setProduct(product1);
        orderItem2.setOrder(savedOrder);
        orderItem2.setProduct(product2);

        testEntityManager.persistAndFlush(orderItem1);
        testEntityManager.persistAndFlush(orderItem2);

        List<OrderItem> items = orderItemRepository.findByOrderId(savedOrder.getId());

        assertThat(items)
            .hasSize(2)
            .allMatch(item -> item.getOrder().getId().equals(savedOrder.getId()))
            .anyMatch(item -> item.getProduct().getName().equals("Laptop"))
            .anyMatch(item -> item.getProduct().getName().equals("Mouse"));
    }

    @Test
    void testFindByOrderId_ShouldReturnEmptyWhenOrderHasNoItems() {
        User savedUser = testEntityManager.persistAndFlush(user);
        order.setUser(savedUser);
        Order savedOrder = testEntityManager.persistAndFlush(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(savedOrder.getId());

        assertThat(items).isEmpty();
    }

    @Test
    void testFindByOrderId_ShouldReturnOnlyItemsForTargetOrder() {
        User savedUser1 = testEntityManager.persistAndFlush(user);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("hashed");
        user2.setRole("USER");
        user2.setStatus("ACTIVE");
        User savedUser2 = testEntityManager.persistAndFlush(user2);

        order.setUser(savedUser1);
        Order savedOrder1 = testEntityManager.persistAndFlush(order);

        Order order2 = new Order();
        order2.setUser(savedUser2);
        order2.setStatus(OrderStatus.CONFIRMED);
        order2.setShippingAddress("456 Oak Ave");
        order2.setTotalAmount(new BigDecimal("100.00"));
        order2.setDiscountAmount(BigDecimal.ZERO);
        order2.setShippingCost(BigDecimal.ZERO);
        order2.setMembershipDiscount(BigDecimal.ZERO);
        Order savedOrder2 = testEntityManager.persistAndFlush(order2);

        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);

        OrderItem item1 = new OrderItem();
        item1.setOrder(savedOrder1);
        item1.setProduct(product1);
        item1.setQuantity(1);
        item1.setUnitPrice(new BigDecimal("999.99"));

        OrderItem item2 = new OrderItem();
        item2.setOrder(savedOrder2);
        item2.setProduct(product2);
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("29.99"));

        testEntityManager.persistAndFlush(item1);
        testEntityManager.persistAndFlush(item2);

        List<OrderItem> order1Items = orderItemRepository.findByOrderId(savedOrder1.getId());

        assertThat(order1Items)
            .hasSize(1)
            .allMatch(item -> item.getOrder().getId().equals(savedOrder1.getId()))
            .anyMatch(item -> item.getProduct().getName().equals("Laptop"));
    }

    @Test
    void testFindByOrderId_ShouldPreserveQuantityAndPrice() {
        User savedUser = testEntityManager.persistAndFlush(user);
        order.setUser(savedUser);
        Order savedOrder = testEntityManager.persistAndFlush(order);

        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);

        orderItem1.setOrder(savedOrder);
        orderItem1.setProduct(product1);
        orderItem1.setQuantity(5);
        orderItem1.setUnitPrice(new BigDecimal("999.99"));

        orderItem2.setOrder(savedOrder);
        orderItem2.setProduct(product2);
        orderItem2.setQuantity(10);
        orderItem2.setUnitPrice(new BigDecimal("29.99"));

        testEntityManager.persistAndFlush(orderItem1);
        testEntityManager.persistAndFlush(orderItem2);

        List<OrderItem> items = orderItemRepository.findByOrderId(savedOrder.getId());

        assertThat(items)
            .anyMatch(item -> item.getQuantity() == 5 && item.getUnitPrice().compareTo(new BigDecimal("999.99")) == 0)
            .anyMatch(item -> item.getQuantity() == 10 && item.getUnitPrice().compareTo(new BigDecimal("29.99")) == 0);
    }

    @Test
    void testFindById_ShouldReturnOrderItem() {
        User savedUser = testEntityManager.persistAndFlush(user);
        order.setUser(savedUser);
        Order savedOrder = testEntityManager.persistAndFlush(order);

        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);

        orderItem1.setOrder(savedOrder);
        orderItem1.setProduct(product1);
        OrderItem saved = testEntityManager.persistAndFlush(orderItem1);

        Optional<OrderItem> found = orderItemRepository.findById(saved.getId());

        assertThat(found)
            .isPresent()
            .hasValueSatisfying(item -> {
                assertThat(item.getQuantity()).isEqualTo(1);
                assertThat(item.getProduct().getName()).isEqualTo("Laptop");
            });
    }

    @Test
    void testFindAll_ShouldReturnAllOrderItems() {
        User savedUser = testEntityManager.persistAndFlush(user);
        order.setUser(savedUser);
        Order savedOrder = testEntityManager.persistAndFlush(order);

        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);

        orderItem1.setOrder(savedOrder);
        orderItem1.setProduct(product1);
        orderItem2.setOrder(savedOrder);
        orderItem2.setProduct(product2);

        testEntityManager.persistAndFlush(orderItem1);
        testEntityManager.persistAndFlush(orderItem2);

        assertThat(orderItemRepository.findAll())
            .hasSizeGreaterThanOrEqualTo(2)
            .anyMatch(item -> item.getId().equals(orderItem1.getId()))
            .anyMatch(item -> item.getId().equals(orderItem2.getId()));
    }

    @Test
    void testCount_ShouldReturnTotalOrderItems() {
        User savedUser = testEntityManager.persistAndFlush(user);
        order.setUser(savedUser);
        Order savedOrder = testEntityManager.persistAndFlush(order);

        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);
        testEntityManager.persistAndFlush(product2);

        orderItem1.setOrder(savedOrder);
        orderItem1.setProduct(product1);
        orderItem2.setOrder(savedOrder);
        orderItem2.setProduct(product2);

        testEntityManager.persistAndFlush(orderItem1);
        testEntityManager.persistAndFlush(orderItem2);

        long count = orderItemRepository.count();

        assertThat(count).isGreaterThanOrEqualTo(2L);
    }

    @Test
    void testUpdate_ShouldModifyExistingOrderItem() {
        User savedUser = testEntityManager.persistAndFlush(user);
        order.setUser(savedUser);
        Order savedOrder = testEntityManager.persistAndFlush(order);

        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);

        orderItem1.setOrder(savedOrder);
        orderItem1.setProduct(product1);
        OrderItem saved = testEntityManager.persistAndFlush(orderItem1);

        saved.setQuantity(5);
        saved.setUnitPrice(new BigDecimal("899.99"));
        orderItemRepository.save(saved);
        testEntityManager.flush();

        OrderItem updated = testEntityManager.find(OrderItem.class, saved.getId());

        assertThat(updated.getQuantity()).isEqualTo(5);
        assertThat(updated.getUnitPrice()).isEqualByComparingTo(new BigDecimal("899.99"));
    }

    @Test
    void testDelete_ShouldRemoveOrderItem() {
        User savedUser = testEntityManager.persistAndFlush(user);
        order.setUser(savedUser);
        Order savedOrder = testEntityManager.persistAndFlush(order);

        testEntityManager.persistAndFlush(category);
        testEntityManager.persistAndFlush(product1);

        orderItem1.setOrder(savedOrder);
        orderItem1.setProduct(product1);
        OrderItem saved = testEntityManager.persistAndFlush(orderItem1);
        Long itemId = saved.getId();

        orderItemRepository.deleteById(itemId);
        testEntityManager.flush();

        OrderItem deleted = testEntityManager.find(OrderItem.class, itemId);

        assertThat(deleted).isNull();
    }
}
