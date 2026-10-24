package com.hcl.portal.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Login Page Object Model
 * Encapsulates UI elements and authentication user interactions on index.html.
 */
public class LoginPage extends BasePage {

    // Locators
    private final By usernameInput = By.id("username");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.id("btn-login");
    private final By alertContainer = By.id("alert-container");
    private final By usernameError = By.id("username-feedback");
    private final By passwordError = By.id("password-feedback");
    private final By appLogo = By.id("app-logo");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage enterUsername(String username) {
        clearAndType(usernameInput, username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        clearAndType(passwordInput, password);
        return this;
    }

    public void clickLogin() {
        click(loginButton);
    }

    /**
     * Helper to perform complete login action
     */
    public DashboardPage loginAsValidUser(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
        return new DashboardPage(driver);
    }

    public LoginPage loginExpectingFailure(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
        return this;
    }

    public String getAlertMessage() {
        return getText(alertContainer);
    }

    public boolean isAlertDisplayed() {
        return isDisplayed(alertContainer);
    }

    public String getUsernameErrorMessage() {
        return getText(usernameError);
    }

    public String getPasswordErrorMessage() {
        return getText(passwordError);
    }

    public boolean isLoaded() {
        return isDisplayed(loginButton) && isDisplayed(usernameInput);
    }
}
