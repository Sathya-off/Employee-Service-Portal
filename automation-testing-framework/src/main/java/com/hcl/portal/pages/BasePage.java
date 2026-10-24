package com.hcl.portal.pages;

import com.hcl.portal.config.ConfigManager;
import com.hcl.portal.utils.WaitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Base Page Object Model (POM) Class
 * Encapsulates common browser actions, locator lookups, and explicit wait synchronization.
 */
public abstract class BasePage {
    protected final Logger logger = LogManager.getLogger(getClass());
    protected final WebDriver driver;
    protected final int defaultTimeout;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.defaultTimeout = ConfigManager.getInstance().getExplicitWait();
    }

    protected WebElement find(By locator) {
        return WaitUtils.waitForVisibility(driver, locator, defaultTimeout);
    }

    protected void click(By locator) {
        logger.debug("Clicking element: {}", locator);
        WebElement element = WaitUtils.waitForClickable(driver, locator, defaultTimeout);
        element.click();
    }

    protected void type(By locator, String text) {
        logger.debug("Typing into element {}: {}", locator, text);
        WebElement element = find(locator);
        element.sendKeys(text);
    }

    protected void clearAndType(By locator, String text) {
        logger.debug("Clearing and typing into element {}: {}", locator, text);
        WebElement element = find(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected void setDateInputValue(By locator, String dateValue) {
        logger.debug("Setting date input {} to '{}'", locator, dateValue);
        WebElement element = find(locator);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "arguments[0].value = arguments[1]; " +
            "arguments[0].dispatchEvent(new Event('input', { bubbles: true })); " +
            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
            element, dateValue
        );
    }

    protected String getText(By locator) {
        WebElement element = find(locator);
        String text = element.getText().trim();
        logger.debug("Retrieved text from {}: '{}'", locator, text);
        return text;
    }

    protected boolean isDisplayed(By locator) {
        try {
            return find(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
