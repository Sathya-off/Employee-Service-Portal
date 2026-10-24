package com.hcl.portal.base;

import com.hcl.portal.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Base Test Class
 * Manages WebDriver setup and teardown for every TestNG test method.
 * Supports configurable browser and headless options via testng.xml or JVM parameters.
 */
public abstract class BaseTest {
    protected final Logger logger = LogManager.getLogger(getClass());
    protected final ConfigManager config = ConfigManager.getInstance();

    @org.testng.annotations.BeforeSuite(alwaysRun = true)
    public void resetApplicationDataBeforeSuite() {
        try {
            io.restassured.RestAssured.given()
                    .post(config.getBaseUrl() + "/api/test/reset");
            logger.info("Application database successfully reset to clean test state before suite run.");
        } catch (Exception e) {
            logger.warn("Could not reset test database: {}", e.getMessage());
        }
    }

    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser", "headless"})
    public void setUp(@Optional("") String browserParam, @Optional("") String headlessParam) {
        String browser = (browserParam != null && !browserParam.isEmpty()) 
                ? browserParam 
                : config.getBrowser();

        boolean headless = (headlessParam != null && !headlessParam.isEmpty()) 
                ? Boolean.parseBoolean(headlessParam) 
                : config.isHeadless();

        logger.info("--- Starting Test Setup | Browser: {} | Headless: {} ---", browser, headless);
        WebDriver driver = DriverFactory.initDriver(browser, headless);
        
        String url = config.getBaseUrl();
        logger.info("Navigating to Portal Base URL: {}", url);
        driver.get(url);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        logger.info("--- Tearing Down Test Execution ---");
        DriverFactory.quitDriver();
    }

    public WebDriver getDriver() {
        return DriverFactory.getDriver();
    }
}
