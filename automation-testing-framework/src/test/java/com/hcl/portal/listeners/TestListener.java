package com.hcl.portal.listeners;

import com.hcl.portal.base.DriverFactory;
import com.hcl.portal.utils.ScreenshotUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG Execution Listener
 * Monitors test lifecycle events, generates structured audit logs,
 * and automatically triggers failure screenshots on test regressions.
 */
public class TestListener implements ITestListener {
    private static final Logger logger = LogManager.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        logger.info("==================================================");
        logger.info("Test Suite Execution Started: {}", context.getName());
        logger.info("==================================================");
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("==================================================");
        logger.info("Test Suite Completed: {}", context.getName());
        logger.info("Passed: {} | Failed: {} | Skipped: {}",
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
        logger.info("==================================================");
    }

    @Override
    public void onTestStart(ITestResult result) {
        logger.info(">>>>> Starting Test: {}.{} <<<<<",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        logger.info(" [PASSED]: {}.{} (Duration: {} ms)",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName(),
                duration);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        logger.error("❌ [FAILED]: {}.{} (Duration: {} ms)",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName(),
                duration);
        logger.error("Failure Cause: {}", result.getThrowable().getMessage());

        WebDriver driver = DriverFactory.getDriver();
        if (driver != null) {
            String screenshotPath = ScreenshotUtil.captureScreenshot(driver, result.getMethod().getMethodName());
            if (screenshotPath != null) {
                logger.info("Evidence Captured: Screenshot stored at {}", screenshotPath);
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("⚠️ [SKIPPED]: {}.{} | Reason: {}",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName(),
                result.getThrowable() != null ? result.getThrowable().getMessage() : "Dependencies failed");
    }
}
