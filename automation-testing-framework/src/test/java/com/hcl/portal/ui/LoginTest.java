package com.hcl.portal.ui;

import com.hcl.portal.base.BaseTest;
import com.hcl.portal.pages.DashboardPage;
import com.hcl.portal.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Authentication Test Suite
 * Verifies positive, negative, boundary, and access-control scenarios for portal login.
 */
public class LoginTest extends BaseTest {

    @Test(priority = 1, groups = {"smoke", "regression"}, description = "Verify successful authentication with valid employee credentials")
    public void testValidLogin() {
        LoginPage loginPage = new LoginPage(getDriver());
        Assert.assertTrue(loginPage.isLoaded(), "Login page should be properly loaded");

        DashboardPage dashboardPage = loginPage.loginAsValidUser(
                config.getEmployeeUsername(),
                config.getEmployeePassword()
        );

        Assert.assertTrue(dashboardPage.isLoaded(), "Dashboard should be loaded after valid login");
        Assert.assertEquals(dashboardPage.getEmployeeId(), config.getEmployeeId(), "Employee ID mismatch on dashboard");
        Assert.assertEquals(dashboardPage.getNavUserName(), config.getEmployeeName(), "Employee name mismatch on navbar");
    }

    @Test(priority = 2, groups = {"regression"}, description = "Verify system rejects valid username with invalid password")
    public void testInvalidPassword() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.loginExpectingFailure(config.getEmployeeUsername(), "WrongPassword!999");

        Assert.assertTrue(loginPage.isAlertDisplayed(), "Error alert should be displayed for invalid password");
        Assert.assertEquals(loginPage.getAlertMessage(), "Invalid username or password", "Incorrect error message displayed");
    }

    @Test(priority = 3, groups = {"regression"}, description = "Verify system rejects non-existent username")
    public void testInvalidUsername() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.loginExpectingFailure("nonexistent.user@hcltech", "AnyPassword#123");

        Assert.assertTrue(loginPage.isAlertDisplayed(), "Error alert should be displayed for non-existent username");
        Assert.assertEquals(loginPage.getAlertMessage(), "Invalid username or password", "Incorrect error message displayed");
    }

    @Test(priority = 4, groups = {"regression"}, description = "Verify client and server validation on submitting empty fields")
    public void testEmptyFieldsValidation() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.clickLogin();

        Assert.assertTrue(loginPage.isAlertDisplayed(), "Error alert should be displayed for empty submissions");
        Assert.assertEquals(loginPage.getAlertMessage(), "Username and password are required", "Incorrect error message for empty submission");
    }

    @Test(priority = 5, groups = {"smoke", "regression"}, description = "Verify employee logout invalidates session and redirects to login")
    public void testLogoutWorkflow() {
        LoginPage loginPage = new LoginPage(getDriver());
        DashboardPage dashboardPage = loginPage.loginAsValidUser(
                config.getEmployeeUsername(),
                config.getEmployeePassword()
        );

        LoginPage returnedLoginPage = dashboardPage.clickLogout();
        Assert.assertTrue(returnedLoginPage.isLoaded(), "Login page should be displayed after logout");
        Assert.assertTrue(returnedLoginPage.isAlertDisplayed(), "Logout success banner should be visible");
        Assert.assertEquals(returnedLoginPage.getAlertMessage(), "You have been logged out successfully.", "Logout confirmation mismatch");
    }

    @Test(priority = 6, groups = {"security", "regression"}, description = "Verify unauthenticated direct URL access to dashboard redirects to login")
    public void testDirectAccessToProtectedDashboardRedirects() {
        // Attempt directly navigating to dashboard without prior authentication
        getDriver().get(config.getBaseUrl() + "/dashboard.html");

        LoginPage loginPage = new LoginPage(getDriver());
        Assert.assertTrue(loginPage.isLoaded(), "Unauthenticated user should be redirected to login page");
        Assert.assertTrue(loginPage.isAlertDisplayed(), "Unauthorized access banner should be displayed");
        Assert.assertEquals(loginPage.getAlertMessage(), "You must log in before accessing protected pages.", "Unauthorized message mismatch");
    }
}
