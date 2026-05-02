package edu.ecommerce.api.controller;

import edu.ecommerce.core.dto.OrderRequest;
import edu.ecommerce.core.dto.OrderItemRequest;
import edu.ecommerce.core.enums.OrderStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.datasource.url=jdbc:h2:mem:copilot;DB_CLOSE_ON_EXIT=FALSE"
)
class OrderControllerTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void testCreateOrder_Success() {
        Long userId = createTestUser();

        OrderItemRequest itemRequest = new OrderItemRequest(1L, 2);
        OrderRequest request = new OrderRequest(userId, List.of(itemRequest), null, null);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/orders")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("status", equalTo("PENDING"))
            .body("items.size()", equalTo(1))
            .body("userId", equalTo(userId.intValue()));
    }

    @Test
    void testCreateOrder_InvalidData_MissingUserId() {
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 2);
        OrderRequest request = new OrderRequest(null, List.of(itemRequest), null, null);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/orders")
            .then()
            .statusCode(400)
            .body("code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    void testCreateOrder_InvalidData_EmptyItems() {
        Long userId = createTestUser();
        OrderRequest request = new OrderRequest(userId, List.of(), null, null);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/orders")
            .then()
            .statusCode(400)
            .body("code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    void testGetOrderById_Success() {
        Long userId = createTestUser();
        Long orderId = createTestOrder(userId);

        given()
            .when()
            .get("/api/v1/orders/{id}", orderId)
            .then()
            .statusCode(200)
            .body("id", equalTo(orderId.intValue()))
            .body("userId", equalTo(userId.intValue()))
            .body("status", equalTo("PENDING"));
    }

    @Test
    void testGetOrderById_NotFound() {
        given()
            .when()
            .get("/api/v1/orders/{id}", 99999L)
            .then()
            .statusCode(404)
            .body("code", equalTo("ORDER_NOT_FOUND"));
    }

    @Test
    void testUpdateOrderStatus_PendingToConfirmed() {
        Long userId = createTestUser();
        Long orderId = createTestOrder(userId);

        String statusRequest = "{\"status\": \"CONFIRMED\"}";

        given()
            .contentType(ContentType.JSON)
            .body(statusRequest)
            .when()
            .patch("/api/v1/orders/{id}/status", orderId)
            .then()
            .statusCode(200)
            .body("status", equalTo("CONFIRMED"));
    }

    @Test
    void testUpdateOrderStatus_InvalidTransition_PendingToShipped() {
        Long userId = createTestUser();
        Long orderId = createTestOrder(userId);

        String statusRequest = "{\"status\": \"SHIPPED\"}";

        given()
            .contentType(ContentType.JSON)
            .body(statusRequest)
            .when()
            .patch("/api/v1/orders/{id}/status", orderId)
            .then()
            .statusCode(400)
            .body("code", equalTo("INVALID_ORDER_TRANSITION"));
    }

    @Test
    void testUpdateOrderStatus_InvalidTransition_FromTerminalState() {
        Long userId = createTestUser();
        Long orderId = createTestOrder(userId);

        cancelOrder(orderId);

        String statusRequest = "{\"status\": \"CONFIRMED\"}";

        given()
            .contentType(ContentType.JSON)
            .body(statusRequest)
            .when()
            .patch("/api/v1/orders/{id}/status", orderId)
            .then()
            .statusCode(400)
            .body("code", equalTo("INVALID_ORDER_TRANSITION"));
    }

    @Test
    void testListAllOrders_Success() {
        Long userId = createTestUser();
        createTestOrder(userId);

        given()
            .when()
            .get("/api/v1/orders")
            .then()
            .statusCode(200)
            .body("content.size()", greaterThanOrEqualTo(1))
            .body("totalElements", greaterThanOrEqualTo(1));
    }

    @Test
    void testCancelOrder_Success() {
        Long userId = createTestUser();
        Long orderId = createTestOrder(userId);

        given()
            .when()
            .post("/api/v1/orders/{id}/cancel", orderId)
            .then()
            .statusCode(200)
            .body("status", equalTo("CANCELLED"));
    }

    private Long createTestUser() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String userRequest = String.format("""
            {
                "username": "testuser_%s",
                "email": "test_%s@example.com",
                "password": "Password123",
                "role": "USER"
            }
            """, uniqueId, uniqueId);

        Integer userId = given()
            .contentType(ContentType.JSON)
            .body(userRequest)
            .when()
            .post("/api/v1/users/register")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        return userId.longValue();
    }

    private Long createTestOrder(Long userId) {
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 1);
        OrderRequest request = new OrderRequest(userId, List.of(itemRequest), null, null);

        Integer orderId = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/orders")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        return orderId.longValue();
    }

    private void cancelOrder(Long orderId) {
        given()
            .when()
            .post("/api/v1/orders/{id}/cancel", orderId)
            .then()
            .statusCode(200);
    }
}
