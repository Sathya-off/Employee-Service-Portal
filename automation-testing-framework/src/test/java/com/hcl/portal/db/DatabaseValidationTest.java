package com.hcl.portal.db;

import com.hcl.portal.config.ConfigManager;
import com.hcl.portal.utils.DatabaseUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Database Validation Test Suite
 * Asserts transactional integrity and persistence across UI, API, and DB tiers.
 */
public class DatabaseValidationTest {
    private final ConfigManager config = ConfigManager.getInstance();
    private String employeeToken;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        RestAssured.baseURI = config.getApiBaseUrl();

        Map<String, String> empPayload = new HashMap<>();
        empPayload.put("username", config.getEmployeeUsername());
        empPayload.put("password", config.getEmployeePassword());
        employeeToken = given().contentType(ContentType.JSON).body(empPayload).post("/auth/login").path("token");
    }

    @Test(priority = 1, groups = {"db", "regression"}, description = "Validate persisted employee records match synthetic HR profiles")
    public void testVerifyPersistedEmployeeRecord() {
        Map<String, Object> emp = DatabaseUtil.getEmployeeById(config.getEmployeeId());

        Assert.assertNotNull(emp, "Persisted employee record should exist in database");
        Assert.assertEquals(emp.get("employeeId"), config.getEmployeeId(), "Database employee_id mismatch");
        Assert.assertEquals(emp.get("name"), config.getEmployeeName(), "Database full_name mismatch");
        Assert.assertEquals(emp.get("department"), "Quality Engineering & Testing", "Database department mismatch");
        Assert.assertTrue((int) emp.get("leaveBalance") >= 0, "Leave balance should be non-negative");
    }

    @Test(priority = 2, groups = {"db", "regression"}, description = "Validate initial historical leave request state in database")
    public void testVerifyPersistedHistoricalLeave() {
        Map<String, Object> leave = DatabaseUtil.getLeaveById(1);

        Assert.assertNotNull(leave, "Leave request #1 should exist in database");
        Assert.assertEquals(leave.get("leaveType"), "CASUAL", "Database leave_type mismatch");
        Assert.assertEquals(leave.get("status"), "APPROVED", "Database leave status mismatch");
        Assert.assertEquals(leave.get("days"), 3, "Database leave days mismatch");
    }

    @Test(priority = 3, groups = {"db", "workflow"}, description = "Cross-layer validation: Submit leave via API and verify record in Database")
    public void testCrossLayerPersistenceIntegrity() {
        // Step 1: Submit leave via API
        Map<String, Object> req = new HashMap<>();
        req.put("leaveType", "EARNED");
        req.put("startDate", "2026-12-28");
        req.put("endDate", "2026-12-30");
        req.put("reason", "Database cross-layer integrity verification");

        Response apiRes = given()
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(ContentType.JSON)
                .body(req)
                .post("/leaves");

        Assert.assertEquals(apiRes.statusCode(), 201, "Leave API should return 201 Created");
        int createdId = apiRes.path("leave.id");

        // Step 2: Query database directly to assert persisted state
        Map<String, Object> dbRecord = DatabaseUtil.getLeaveById(createdId);
        Assert.assertNotNull(dbRecord, "Newly created leave must exist in persistent database");
        Assert.assertEquals(dbRecord.get("leaveType"), "EARNED", "Database leave_type mismatch");
        Assert.assertEquals(dbRecord.get("startDate"), "2026-12-28", "Database start_date mismatch");
        Assert.assertEquals(dbRecord.get("endDate"), "2026-12-30", "Database end_date mismatch");
        Assert.assertEquals(dbRecord.get("status"), "PENDING", "Database status mismatch");
        Assert.assertEquals(dbRecord.get("days"), 3, "Database calculated days mismatch");
    }
}
