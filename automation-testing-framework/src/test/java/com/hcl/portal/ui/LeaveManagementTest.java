package com.hcl.portal.ui;

import com.hcl.portal.base.BaseTest;
import com.hcl.portal.pages.DashboardPage;
import com.hcl.portal.pages.LeavePage;
import com.hcl.portal.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Leave Management UI Test Suite
 * Validates leave submissions, boundary date rules, required field validations,
 * history tracking, and the manager approval workflow.
 */
public class LeaveManagementTest extends BaseTest {

    @Test(priority = 1, groups = {"smoke", "regression"}, description = "Verify successful submission of valid leave request")
    public void testSubmitValidLeaveRequest() {
        LoginPage loginPage = new LoginPage(getDriver());
        DashboardPage dashboardPage = loginPage.loginAsValidUser(
                config.getEmployeeUsername(),
                config.getEmployeePassword()
        );

        LeavePage leavePage = dashboardPage.navigateToLeaveManagement();
        Assert.assertTrue(leavePage.isLoaded(), "Leave management page should be loaded");

        // Submit new valid request
        leavePage.applyForLeave(
                "CASUAL",
                "2026-12-15",
                "2026-12-16",
                "Attending automation engineering symposium"
        );

        Assert.assertTrue(leavePage.isAlertDisplayed(), "Confirmation banner should appear after submission");
        Assert.assertTrue(leavePage.getAlertMessage().contains("submitted successfully"), 
                "Success message mismatch: " + leavePage.getAlertMessage());
    }

    @Test(priority = 2, groups = {"regression"}, description = "Verify validation rejects end date earlier than start date")
    public void testRejectInvalidDateRange() {
        LoginPage loginPage = new LoginPage(getDriver());
        DashboardPage dashboardPage = loginPage.loginAsValidUser(
                config.getEmployeeUsername(),
                config.getEmployeePassword()
        );

        LeavePage leavePage = dashboardPage.navigateToLeaveManagement();
        leavePage.applyForLeave(
                "SICK",
                "2026-11-20",
                "2026-11-15",
                "Invalid dates test"
        );

        Assert.assertTrue(leavePage.isAlertDisplayed(), "Error alert must appear when end date is earlier than start date");
        Assert.assertEquals(leavePage.getAlertMessage(), "End date cannot be earlier than start date");
    }

    @Test(priority = 3, groups = {"regression"}, description = "Verify form rejects submission when required fields are missing")
    public void testRejectMissingRequiredFields() {
        LoginPage loginPage = new LoginPage(getDriver());
        DashboardPage dashboardPage = loginPage.loginAsValidUser(
                config.getEmployeeUsername(),
                config.getEmployeePassword()
        );

        LeavePage leavePage = dashboardPage.navigateToLeaveManagement();
        leavePage.clickSubmit();

        Assert.assertTrue(leavePage.isAlertDisplayed(), "Validation alert must appear when required fields are blank");
        Assert.assertEquals(leavePage.getAlertMessage(), "Please complete all required fields.");
    }

    @Test(priority = 4, groups = {"regression"}, description = "Verify leave history table displays historical records and statuses")
    public void testLeaveHistoryTablePopulated() {
        LoginPage loginPage = new LoginPage(getDriver());
        DashboardPage dashboardPage = loginPage.loginAsValidUser(
                config.getEmployeeUsername(),
                config.getEmployeePassword()
        );

        LeavePage leavePage = dashboardPage.navigateToLeaveManagement();
        Assert.assertTrue(leavePage.isLeaveHistoryTableDisplayed(), "Leave history table must be displayed");
        Assert.assertTrue(leavePage.isLeaveRecordPresent(1), "Leave record #1 should be present in history table");
        Assert.assertEquals(leavePage.getLeaveStatusBadgeText(1), "APPROVED", "Status for initial approved seed record mismatch");
    }
}
