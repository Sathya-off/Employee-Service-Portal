package com.hcl.portal.api;

import com.hcl.portal.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * REST Assured Authentication API Test Suite
 * Validates HTTP status codes, response payload schemas, error messages, and session token generation.
 */
public class AuthApiTest {
    private final ConfigManager config = ConfigManager.getInstance();

    @BeforeClass(alwaysRun = true)
    public void setupApi() {
        RestAssured.baseURI = config.getApiBaseUrl();
    }

    @Test(priority = 1, groups = {"api", "smoke"}, description = "POST /api/auth/login - Valid credentials return HTTP 200, token, and user object")
    public void testValidLoginApi() {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", config.getEmployeeUsername());
        payload.put("password", config.getEmployeePassword());

        Response response = given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("success", equalTo(true))
                .body("message", equalTo("Authentication successful"))
                .body("token", notNullValue())
                .body("user.employeeId", equalTo(config.getEmployeeId()))
                .body("user.name", equalTo(config.getEmployeeName()))
                .body("user.role", equalTo("EMPLOYEE"))
                .extract().response();

        String token = response.path("token");
        Assert.assertFalse(token.isEmpty(), "Token should not be empty in successful response");
    }

    @Test(priority = 2, groups = {"api", "regression"}, description = "POST /api/auth/login - Invalid password returns HTTP 401 Unauthorized")
    public void testInvalidPasswordApi() {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", config.getEmployeeUsername());
        payload.put("password", "IncorrectPassword123!");

        given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(401)
                .contentType(ContentType.JSON)
                .body("success", equalTo(false))
                .body("message", equalTo("Invalid username or password"));
    }

    @Test(priority = 3, groups = {"api", "regression"}, description = "POST /api/auth/login - Empty credentials return HTTP 400 Bad Request")
    public void testEmptyCredentialsApi() {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", "");
        payload.put("password", "");

        given()
                .contentType(ContentType.JSON)
                .body(payload)
        .when()
                .post("/auth/login")
        .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("success", equalTo(false))
                .body("message", equalTo("Username and password are required"));
    }

    @Test(priority = 4, groups = {"api", "regression"}, description = "POST /api/auth/logout - Valid token logout returns HTTP 200")
    public void testLogoutApi() {
        // Authenticate first
        Map<String, String> payload = new HashMap<>();
        payload.put("username", config.getEmployeeUsername());
        payload.put("password", config.getEmployeePassword());

        String token = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/auth/login")
                .path("token");

        // Call logout
        given()
                .header("Authorization", "Bearer " + token)
        .when()
                .post("/auth/logout")
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Logged out successfully"));
    }
}
