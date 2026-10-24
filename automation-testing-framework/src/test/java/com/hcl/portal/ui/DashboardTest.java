package com.hcl.portal.ui;

import com.hcl.portal.base.BaseTest;
import com.hcl.portal.pages.DashboardPage;
import com.hcl.portal.pages.LeavePage;
import com.hcl.portal.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Employee Dashboard UI Test Suite
 * Verifies profile attributes display, quota widgets, navigation flows, and recent activity.
 */
public class DashboardTest extends BaseTest {

    private DashboardPage dashboardPage;

    @BeforeMethod(alwaysRun = true)
    public void loginBeforeTest() {
        LoginPage loginPage = new LoginPage(getDriver());
        dashboardPage = loginPage.loginAsValidUser(
                config.getEmployeeUsername(),
                config.getEmployeePassword()
        );
        Assert.assertTrue(dashboardPage.isLoaded(), "Precondition failed: Dashboard failed to load after login");
    }

    @Test(priority = 1, groups = {"smoke", "regression"}, description = "Verify dashboard metric cards load and display accurate employee values")
    public void testDashboardMetricCardsDisplay() {
        Assert.assertEquals(dashboardPage.getEmployeeId(), config.getEmployeeId(), "Employee ID card mismatch");
        Assert.assertEquals(dashboardPage.getDepartment(), "Quality Engineering & Testing", "Department card mismatch");
        Assert.assertTrue(dashboardPage.getLeaveBalance().contains("Days"), "Leave balance quota format mismatch");
    }

    @Test(priority = 2, groups = {"regression"}, description = "Verify comprehensive employee profile table matches synthetic HR records")
    public void testEmployeeProfileDetailsDisplay() {
        Assert.assertEquals(dashboardPage.getProfileName(), config.getEmployeeName(), "Profile name mismatch");
        Assert.assertEquals(dashboardPage.getProfileDesignation(), "Associate Test Engineer", "Designation mismatch");
        Assert.assertEquals(dashboardPage.getProfileEmail(), "sathya.narayan@hcltech-demo.com", "Email mismatch");
    }

    @Test(priority = 3, groups = {"regression"}, description = "Verify top navbar navigation links route properly to Leave Management")
    public void testNavbarNavigationToLeaveManagement() {
        LeavePage leavePage = dashboardPage.navigateToLeaveManagement();
        Assert.assertTrue(leavePage.isLoaded(), "Leave management page should load after clicking navbar link");
        Assert.assertTrue(leavePage.getCurrentUrl().contains("leave-management.html"), "URL should contain leave-management.html");
    }

    @Test(priority = 4, groups = {"regression"}, description = "Verify dashboard quick action '+ Apply for Leave' navigates to Leave form")
    public void testQuickActionApplyLeaveNavigation() {
        LeavePage leavePage = dashboardPage.clickQuickApplyLeave();
        Assert.assertTrue(leavePage.isLoaded(), "Quick Apply Leave should route to Leave Management page");
    }

    @Test(priority = 5, groups = {"regression"}, description = "Verify recent leaves summary widget is populated")
    public void testRecentLeavesSummaryWidget() {
        Assert.assertTrue(dashboardPage.isRecentLeavesTableDisplayed(), "Recent leaves table should be visible on dashboard");
    }
}
