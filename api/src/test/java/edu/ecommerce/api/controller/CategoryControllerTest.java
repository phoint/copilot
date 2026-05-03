package edu.ecommerce.api.controller;

import edu.ecommerce.core.dto.CategoryRequest;
import edu.ecommerce.core.dto.CategoryUpdateRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.datasource.url=jdbc:h2:mem:copilot;DB_CLOSE_ON_EXIT=FALSE"
)
class CategoryControllerTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void testCreateCategory_Success() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        CategoryRequest request = new CategoryRequest();
        request.setName("Category_" + uniqueId);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/categories")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Category_" + uniqueId))
            .body("parentId", nullValue());
    }

    @Test
    void testCreateCategory_WithParent() {
        Long parentId = createTestCategory("Parent_Category");

        CategoryRequest request = new CategoryRequest();
        request.setName("Child_Category");
        request.setParentId(parentId);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/categories")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Child_Category"))
            .body("parentId", equalTo(parentId.intValue()));
    }

    @Test
    void testCreateCategory_InvalidParent() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Invalid_Child");
        request.setParentId(99999L);

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/categories")
            .then()
            .statusCode(404);
    }

    @Test
    void testGetCategoryById_Success() {
        Long categoryId = createTestCategory("Test_Category");

        given()
            .when()
            .get("/api/v1/categories/{id}", categoryId)
            .then()
            .statusCode(200)
            .body("id", equalTo(categoryId.intValue()))
            .body("name", equalTo("Test_Category"))
            .body("parentId", nullValue());
    }

    @Test
    void testGetCategoryById_NotFound() {
        given()
            .when()
            .get("/api/v1/categories/{id}", 99999L)
            .then()
            .statusCode(404);
    }

    @Test
    void testListAllCategories_Success() {
        createTestCategory("Category_1");
        createTestCategory("Category_2");

        given()
            .when()
            .get("/api/v1/categories")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    void testUpdateCategory_OnlyName() {
        Long categoryId = createTestCategory("Original_Name");

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("Updated_Name");

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/v1/categories/{id}", categoryId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated_Name"));
    }

    @Test
    void testUpdateCategory_ChangeParent() {
        Long parentId = createTestCategory("Parent_Category");
        Long childId = createTestCategory("Child_Category");

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setParentId(parentId);

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/v1/categories/{id}", childId)
            .then()
            .statusCode(200)
            .body("parentId", equalTo(parentId.intValue()));
    }

    @Test
    void testUpdateCategory_NotFound() {
        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("Updated_Name");

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/v1/categories/{id}", 99999L)
            .then()
            .statusCode(404);
    }

    @Test
    void testUpdateCategory_InvalidParent() {
        Long categoryId = createTestCategory("Test_Category");

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setParentId(99999L);

        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .put("/api/v1/categories/{id}", categoryId)
            .then()
            .statusCode(404);
    }

    @Test
    void testDeleteCategory_Success() {
        Long categoryId = createTestCategory("To_Delete");

        given()
            .when()
            .delete("/api/v1/categories/{id}", categoryId)
            .then()
            .statusCode(204);

        // Verify it's deleted
        given()
            .when()
            .get("/api/v1/categories/{id}", categoryId)
            .then()
            .statusCode(404);
    }

    @Test
    void testDeleteCategory_NotFound() {
        given()
            .when()
            .delete("/api/v1/categories/{id}", 99999L)
            .then()
            .statusCode(404);
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
}
