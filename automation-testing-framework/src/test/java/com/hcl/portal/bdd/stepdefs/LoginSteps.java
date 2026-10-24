package com.hcl.portal.bdd.stepdefs;

import com.hcl.portal.base.DriverFactory;
import com.hcl.portal.config.ConfigManager;
import com.hcl.portal.pages.DashboardPage;
import com.hcl.portal.pages.LoginPage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

/**
 * Cucumber BDD Step Definitions for Authentication Scenarios
 */
public class LoginSteps {
    private WebDriver driver;
    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    @Before
    public void setupScenario() {
        ConfigManager cfg = ConfigManager.getInstance();
        driver = DriverFactory.initDriver(cfg.getBrowser(), cfg.isHeadless());
    }

    @After
    public void teardownScenario() {
        DriverFactory.quitDriver();
    }

    @Given("the user navigates to the portal login page")
    public void userNavigatesToLoginPage() {
        driver.get(ConfigManager.getInstance().getBaseUrl());
        loginPage = new LoginPage(driver);
        Assert.assertTrue(loginPage.isLoaded(), "Login page failed to load");
    }

    @When("the user enters username {string} and password {string}")
    public void userEntersCredentials(String username, String password) {
        if (!username.isEmpty()) {
            loginPage.enterUsername(username);
        }
        if (!password.isEmpty()) {
            loginPage.enterPassword(password);
        }
    }

    @And("clicks on the login button")
    public void userClicksLogin() {
        loginPage.clickLogin();
    }

    @Then("the user should be redirected to the employee dashboard")
    public void userRedirectedToDashboard() {
        dashboardPage = new DashboardPage(driver);
        Assert.assertTrue(dashboardPage.isLoaded(), "Dashboard failed to load after login");
    }

    @And("the employee ID should be displayed as {string}")
    public void employeeIdDisplayed(String expectedId) {
        Assert.assertEquals(dashboardPage.getEmployeeId(), expectedId, "Employee ID mismatch");
    }

    @Then("an error alert should be displayed with message {string}")
    public void errorAlertDisplayed(String expectedMessage) {
        Assert.assertTrue(loginPage.isAlertDisplayed(), "Error alert should be displayed");
        Assert.assertEquals(loginPage.getAlertMessage(), expectedMessage, "Error message mismatch");
    }
}
