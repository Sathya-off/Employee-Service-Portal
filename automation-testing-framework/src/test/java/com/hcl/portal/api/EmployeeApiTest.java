package com.hcl.portal.api;

import com.hcl.portal.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * REST Assured Employee API Test Suite
 * Validates profile retrieval, authorization gates, and 404 boundary handling.
 */
public class EmployeeApiTest {
    private final ConfigManager config = ConfigManager.getInstance();
    private String authToken;

    @BeforeClass(alwaysRun = true)
    public void setupApi() {
        RestAssured.baseURI = config.getApiBaseUrl();

        // Obtain valid token for authorized calls
        Map<String, String> payload = new HashMap<>();
        payload.put("username", config.getEmployeeUsername());
        payload.put("password", config.getEmployeePassword());

        authToken = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/auth/login")
                .path("token");
    }

    @Test(priority = 1, groups = {"api", "smoke"}, description = "GET /api/employees/me - Authorized request retrieves current employee profile")
    public void testGetCurrentEmployeeProfileAuthorized() {
        given()
                .header("Authorization", "Bearer " + authToken)
        .when()
                .get("/employees/me")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("success", equalTo(true))
                .body("employee.employeeId", equalTo(config.getEmployeeId()))
                .body("employee.name", equalTo(config.getEmployeeName()))
                .body("employee.department", equalTo("Quality Engineering & Testing"))
                .body("employee.leaveBalance", greaterThanOrEqualTo(0));
    }

    @Test(priority = 2, groups = {"api", "security"}, description = "GET /api/employees/me - Unauthenticated request without token returns HTTP 401")
    public void testGetCurrentEmployeeProfileUnauthorized() {
        given()
        .when()
                .get("/employees/me")
        .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", containsString("Unauthorized"));
    }

    @Test(priority = 3, groups = {"api", "regression"}, description = "GET /api/employees/{id} - Valid employee ID retrieves record")
    public void testGetEmployeeById() {
        given()
                .header("Authorization", "Bearer " + authToken)
        .when()
                .get("/employees/" + config.getEmployeeId())
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("employee.employeeId", equalTo(config.getEmployeeId()))
                .body("employee.name", equalTo(config.getEmployeeName()));
    }

    @Test(priority = 4, groups = {"api", "regression"}, description = "GET /api/employees/{id} - Non-existent employee returns HTTP 404")
    public void testGetNonExistentEmployeeReturns404() {
        given()
                .header("Authorization", "Bearer " + authToken)
        .when()
                .get("/employees/INVALID_ID_99999")
        .then()
                .statusCode(404)
                .body("success", equalTo(false))
                .body("message", containsString("not found"));
    }
}
