package com.hcl.portal.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Screenshot Capture Utility
 * Captures browser viewport on test failure for auditability and failure analysis.
 */
public class ScreenshotUtil {
    private static final Logger logger = LogManager.getLogger(ScreenshotUtil.class);
    private static final String SCREENSHOTS_DIR = "screenshots";

    public static String captureScreenshot(WebDriver driver, String testName) {
        if (driver == null) {
            logger.warn("Driver is null; cannot capture screenshot for: {}", testName);
            return null;
        }

        try {
            File dir = new File(SCREENSHOTS_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            String sanitizedTestName = testName.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = String.format("%s_%s.png", sanitizedTestName, timestamp);
            File destinationFile = new File(dir, fileName);

            File sourceFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            logger.info("Screenshot saved successfully: {}", destinationFile.getAbsolutePath());
            return destinationFile.getAbsolutePath();
        } catch (IOException e) {
            logger.error("Failed to capture screenshot for test {}: {}", testName, e.getMessage(), e);
            return null;
        }
    }
}
