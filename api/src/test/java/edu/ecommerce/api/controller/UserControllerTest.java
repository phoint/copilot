package edu.ecommerce.api.controller;

import edu.ecommerce.core.dto.UserRequest;
import edu.ecommerce.core.dto.UserResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_ON_EXIT=FALSE"}
)
public class UserControllerTest {
    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/users";
    }

    private UserRequest createUserRequest(String suffix) {
        UserRequest request = new UserRequest();
        request.setUsername("testuser" + suffix);
        request.setEmail("test" + suffix + "@example.com");
        request.setPassword("password123");
        request.setRole("USER");
        return request;
    }

    @Test
    public void testCreateUser_Success() {
        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(createUserRequest("_success"))
        .when()
            .post("/register")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("username", is("testuser_success"))
            .body("email", is("test_success@example.com"))
            .body("role", is("USER"))
            .body("status", is("ACTIVE"));
    }

    @Test
    public void testCreateUser_InvalidEmail() {
        // given
        UserRequest request = createUserRequest("_invalid_email");
        request.setEmail("invalid-email");

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/register")
        .then()
            .statusCode(400)
            .body("code", is("VALIDATION_ERROR"));
    }

    @Test
    public void testCreateUser_MissingUsername() {
        // given
        UserRequest request = createUserRequest("_missing_username");
        request.setUsername("");

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/register")
        .then()
            .statusCode(400)
            .body("code", is("VALIDATION_ERROR"));
    }

    @Test
    public void testGetUserById_Success() {
        // given - create a user first
        Integer userId = given()
            .contentType(ContentType.JSON)
            .body(createUserRequest("_get_by_id"))
        .when()
            .post("/register")
        .then()
            .statusCode(201)
            .extract().path("id");

        // when & then
        when()
            .get("/{id}", userId)
        .then()
            .statusCode(200)
            .body("id", is(userId))
            .body("username", is("testuser_get_by_id"))
            .body("email", is("test_get_by_id@example.com"));
    }

    @Test
    public void testGetUserById_NotFound() {
        // when & then
        when()
            .get("/{id}", 999)
        .then()
            .statusCode(404)
            .body("code", is("USER_NOT_FOUND"));
    }

    @Test
    public void testUpdateUser_Success() {
        // given - create a user first
        Integer userId = given()
            .contentType(ContentType.JSON)
            .body(createUserRequest("_update"))
        .when()
            .post("/register")
        .then()
            .statusCode(201)
            .extract().path("id");

        // given - prepare update request
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("updateduser_update");
        updateRequest.setEmail("updated_update@example.com");
        updateRequest.setPassword("newpassword123");
        updateRequest.setRole("ADMIN");

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/{id}", userId)
        .then()
            .statusCode(200)
            .body("username", is("updateduser_update"))
            .body("email", is("updated_update@example.com"));
    }

    @Test
    public void testDeleteUser_Success() {
        // given - create a user first
        Integer userId = given()
            .contentType(ContentType.JSON)
            .body(createUserRequest("_delete"))
        .when()
            .post("/register")
        .then()
            .statusCode(201)
            .extract().path("id");

        // when & then
        when()
            .delete("/{id}", userId)
        .then()
            .statusCode(204);

        // verify user is deleted
        when()
            .get("/{id}", userId)
        .then()
            .statusCode(404);
    }

    @Test
    public void testListAllUsers_Success() {
        // given - create a user
        given()
            .contentType(ContentType.JSON)
            .body(createUserRequest("_list1"))
        .when()
            .post("/register")
        .then()
            .statusCode(201);

        // when & then
        when()
            .get()
        .then()
            .statusCode(200)
            .body("content.size()", greaterThanOrEqualTo(1))
            .body("totalElements", greaterThanOrEqualTo(1));
    }

    @Test
    public void testGetUserByEmail_Success() {
        // given - create a user first
        given()
            .contentType(ContentType.JSON)
            .body(createUserRequest("_email"))
        .when()
            .post("/register")
        .then()
            .statusCode(201);

        // when & then
        when()
            .get("email/{email}", "test_email@example.com")
        .then()
            .statusCode(200)
            .body("email", is("test_email@example.com"))
            .body("username", is("testuser_email"));
    }

    @Test
    public void testCreateUser_DuplicateEmail() {
        // given - create first user
        given()
            .contentType(ContentType.JSON)
            .body(createUserRequest("_duplicate"))
        .when()
            .post("/register")
        .then()
            .statusCode(201);

        // when & then - try to create duplicate
        given()
            .contentType(ContentType.JSON)
            .body(createUserRequest("_duplicate"))
        .when()
            .post("/register")
        .then()
            .statusCode(409)
            .body("code", is("DUPLICATE_EMAIL"));
    }
}
