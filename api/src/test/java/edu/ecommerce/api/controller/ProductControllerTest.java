package edu.ecommerce.api.controller;

import edu.ecommerce.core.dto.CategoryRequest;
import edu.ecommerce.core.dto.ProductRequest;
import edu.ecommerce.core.dto.ProductUpdateRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.datasource.url=jdbc:h2:mem:copilot;DB_CLOSE_ON_EXIT=FALSE"
)
class ProductControllerTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void testCreateProduct_Success() {
        Long categoryId = createTestCategory("Electronics");

        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setDescription("Test Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setStockQuantity(100);
        request.setCategoryId(categoryId);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/products")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Test Product"))
            .body("description", equalTo("Test Description"))
            .body("price", notNullValue())
            .body("stockQuantity", equalTo(100))
            .body("categoryId", equalTo(categoryId.intValue()));
    }

    @Test
    void testCreateProduct_InvalidCategory() {
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setDescription("Test Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setStockQuantity(100);
        request.setCategoryId(99999L);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/products")
            .then()
            .statusCode(404);
    }

    @Test
    void testCreateProduct_MultipleProducts() {
        Long categoryId = createTestCategory("Smartphones");

        String uniqueId1 = UUID.randomUUID().toString().substring(0, 8);
        ProductRequest request1 = new ProductRequest();
        request1.setName("Product_" + uniqueId1);
        request1.setDescription("Description 1");
        request1.setPrice(new BigDecimal("299.99"));
        request1.setStockQuantity(50);
        request1.setCategoryId(categoryId);

        given()
            .contentType(ContentType.JSON)
            .body(request1)
            .when()
            .post("/api/v1/products")
            .then()
            .statusCode(201);

        String uniqueId2 = UUID.randomUUID().toString().substring(0, 8);
        ProductRequest request2 = new ProductRequest();
        request2.setName("Product_" + uniqueId2);
        request2.setDescription("Description 2");
        request2.setPrice(new BigDecimal("399.99"));
        request2.setStockQuantity(30);
        request2.setCategoryId(categoryId);

        given()
            .contentType(ContentType.JSON)
            .body(request2)
            .when()
            .post("/api/v1/products")
            .then()
            .statusCode(201);
    }

    @Test
    void testGetProductById_Success() {
        Long productId = createTestProduct("Test_Product");

        given()
            .when()
            .get("/api/v1/products/{id}", productId)
            .then()
            .statusCode(200)
            .body("id", equalTo(productId.intValue()))
            .body("name", equalTo("Test_Product"));
    }

    @Test
    void testGetProductById_NotFound() {
        given()
            .when()
            .get("/api/v1/products/{id}", 99999L)
            .then()
            .statusCode(404);
    }

    @Test
    void testListAllProducts_Success() {
        createTestProduct("Product_1");
        createTestProduct("Product_2");

        given()
            .when()
            .get("/api/v1/products")
            .then()
            .statusCode(200)
            .body("content.size()", greaterThanOrEqualTo(2))
            .body("totalElements", greaterThanOrEqualTo(2));
    }

    @Test
    void testGetAllProducts_Success() {
        createTestProduct("Product_A");

        given()
            .when()
            .get("/api/v1/products/all")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void testUpdateProduct_OnlyName() {
        Long productId = createTestProduct("Original_Name");

        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("Updated_Name");

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/v1/products/{id}", productId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated_Name"));
    }

    @Test
    void testUpdateProduct_OnlyPrice() {
        Long productId = createTestProduct("Test_Product");

        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setPrice(new BigDecimal("199.99"));

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/v1/products/{id}", productId)
            .then()
            .statusCode(200)
            .body("price", notNullValue());
    }

    @Test
    void testUpdateProduct_OnlyStockQuantity() {
        Long productId = createTestProduct("Test_Product");

        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setStockQuantity(200);

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/v1/products/{id}", productId)
            .then()
            .statusCode(200)
            .body("stockQuantity", equalTo(200));
    }

    @Test
    void testUpdateProduct_ChangeCategory() {
        Long oldCategoryId = createTestCategory("Old_Category");
        Long newCategoryId = createTestCategory("New_Category");
        Long productId = createTestProductWithCategory("Test_Product", oldCategoryId);

        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setCategoryId(newCategoryId);

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/v1/products/{id}", productId)
            .then()
            .statusCode(200)
            .body("categoryId", equalTo(newCategoryId.intValue()));
    }

    @Test
    void testUpdateProduct_AllFields() {
        Long categoryId = createTestCategory("Updated_Category");
        Long productId = createTestProduct("Original_Product");

        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("Updated_Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(new BigDecimal("499.99"));
        updateRequest.setStockQuantity(150);
        updateRequest.setCategoryId(categoryId);

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/v1/products/{id}", productId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated_Product"))
            .body("stockQuantity", equalTo(150));
    }

    @Test
    void testUpdateProduct_NotFound() {
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("Updated_Name");

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/v1/products/{id}", 99999L)
            .then()
            .statusCode(404);
    }

    @Test
    void testUpdateProduct_InvalidCategory() {
        Long productId = createTestProduct("Test_Product");

        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setCategoryId(99999L);

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/v1/products/{id}", productId)
            .then()
            .statusCode(404);
    }

    @Test
    void testDeleteProduct_Success() {
        Long productId = createTestProduct("To_Delete");

        given()
            .when()
            .delete("/api/v1/products/{id}", productId)
            .then()
            .statusCode(204);

        // Verify it's deleted
        given()
            .when()
            .get("/api/v1/products/{id}", productId)
            .then()
            .statusCode(404);
    }

    @Test
    void testDeleteProduct_NotFound() {
        given()
            .when()
            .delete("/api/v1/products/{id}", 99999L)
            .then()
            .statusCode(404);
    }

    @Test
    void testSearchProducts_ByKeyword() {
        createTestProduct("iPhone_13");
        createTestProduct("iPhone_14");
        createTestProduct("Samsung_S23");

        given()
            .param("keyword", "iPhone")
            .when()
            .get("/api/v1/products/search")
            .then()
            .statusCode(200)
            .body("content.size()", greaterThanOrEqualTo(2));
    }

    @Test
    void testSearchProducts_ByPriceRange() {
        Long categoryId = createTestCategory("Electronics");

        ProductRequest request1 = new ProductRequest();
        request1.setName("Expensive_Product");
        request1.setPrice(new BigDecimal("999.99"));
        request1.setStockQuantity(10);
        request1.setCategoryId(categoryId);

        given()
            .contentType(ContentType.JSON)
            .body(request1)
            .when()
            .post("/api/v1/products")
            .then()
            .statusCode(201);

        given()
            .param("minPrice", "500")
            .param("maxPrice", "1500")
            .when()
            .get("/api/v1/products/search")
            .then()
            .statusCode(200)
            .body("content.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void testSearchProducts_Empty() {
        given()
            .param("keyword", "NonExistentProduct_XYZ")
            .when()
            .get("/api/v1/products/search")
            .then()
            .statusCode(200)
            .body("content.size()", equalTo(0));
    }

    private Long createTestCategory(String name) {
        CategoryRequest request = new CategoryRequest();
        request.setName(name);

        Integer categoryId = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/categories")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        return categoryId.longValue();
    }

    private Long createTestProduct(String name) {
        Long categoryId = createTestCategory("Default_Category");
        return createTestProductWithCategory(name, categoryId);
    }

    private Long createTestProductWithCategory(String name, Long categoryId) {
        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setDescription("Test Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setStockQuantity(100);
        request.setCategoryId(categoryId);

        Integer productId = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/products")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        return productId.longValue();
    }
}
