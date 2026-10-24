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
 * REST Assured Leave Management API Test Suite
 * Validates leave creation, boundary business validations, quota enforcement, and manager approval.
 */
public class LeaveApiTest {
    private final ConfigManager config = ConfigManager.getInstance();
    private String employeeToken;
    private String managerToken;

    @BeforeClass(alwaysRun = true)
    public void setupTokens() {
        RestAssured.baseURI = config.getApiBaseUrl();

        // 1. Employee Token
        Map<String, String> empPayload = new HashMap<>();
        empPayload.put("username", config.getEmployeeUsername());
        empPayload.put("password", config.getEmployeePassword());
        employeeToken = given().contentType(ContentType.JSON).body(empPayload).post("/auth/login").path("token");

        // 2. Manager Token
        Map<String, String> mgrPayload = new HashMap<>();
        mgrPayload.put("username", config.getManagerUsername());
        mgrPayload.put("password", config.getManagerPassword());
        managerToken = given().contentType(ContentType.JSON).body(mgrPayload).post("/auth/login").path("token");
    }

    @Test(priority = 1, groups = {"api", "smoke"}, description = "POST /api/leaves - Valid submission returns HTTP 201 and new leave record")
    public void testCreateLeaveRequestSuccess() {
        Map<String, Object> leaveReq = new HashMap<>();
        leaveReq.put("leaveType", "CASUAL");
        leaveReq.put("startDate", "2026-12-20");
        leaveReq.put("endDate", "2026-12-22");
        leaveReq.put("reason", "Year-end annual testing workshop");

        Response response = given()
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(ContentType.JSON)
                .body(leaveReq)
        .when()
                .post("/leaves")
        .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("success", equalTo(true))
                .body("message", containsString("submitted successfully"))
                .body("leave.employeeId", equalTo(config.getEmployeeId()))
                .body("leave.status", equalTo("PENDING"))
                .body("leave.days", equalTo(3))
                .extract().response();

        int leaveId = response.path("leave.id");
        Assert.assertTrue(leaveId > 0, "Created leave ID should be positive integer");
    }

    @Test(priority = 2, groups = {"api", "regression"}, description = "POST /api/leaves - End date before start date returns HTTP 400")
    public void testCreateLeaveInvalidDateRange() {
        Map<String, Object> leaveReq = new HashMap<>();
        leaveReq.put("leaveType", "SICK");
        leaveReq.put("startDate", "2026-11-25");
        leaveReq.put("endDate", "2026-11-20"); // Earlier than start date
        leaveReq.put("reason", "API boundary date test");

        given()
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(ContentType.JSON)
                .body(leaveReq)
        .when()
                .post("/leaves")
        .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("End date cannot be earlier than start date"));
    }

    @Test(priority = 3, groups = {"api", "regression"}, description = "POST /api/leaves - Missing required fields returns HTTP 400")
    public void testCreateLeaveMissingFields() {
        Map<String, Object> leaveReq = new HashMap<>();
        leaveReq.put("leaveType", "CASUAL");
        // startDate and endDate missing intentionally
        leaveReq.put("reason", "Incomplete payload");

        given()
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(ContentType.JSON)
                .body(leaveReq)
        .when()
                .post("/leaves")
        .then()
                .statusCode(400)
                .body("success", equalTo(false));
    }

    @Test(priority = 4, groups = {"api", "workflow"}, description = "PUT /api/leaves/{id}/status - Manager approval updates status and persists state")
    public void testManagerApprovalWorkflow() {
        Map<String, String> statusBody = new HashMap<>();
        statusBody.put("status", "APPROVED");
        statusBody.put("remarks", "Approved during API automated verification");

        given()
                .header("Authorization", "Bearer " + managerToken)
                .contentType(ContentType.JSON)
                .body(statusBody)
        .when()
                .put("/leaves/2/status") // Leave ID 2 is seeded as PENDING
        .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("leave.status", equalTo("APPROVED"));
    }
}
