package com.hcl.portal.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton Configuration Manager
 * Loads environment configurations from config.properties with
 * support for system property overrides (e.g., -Dbrowser=edge -Dheadless=false).
 */
public class ConfigManager {
    private static final Logger logger = LogManager.getLogger(ConfigManager.class);
    private static ConfigManager instance;
    private final Properties properties = new Properties();

    private ConfigManager() {
        loadProperties();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                properties.load(is);
                logger.info("Loaded configuration properties successfully.");
            } else {
                logger.warn("config.properties file not found in classpath. Relying on default fallbacks.");
            }
        } catch (Exception e) {
            logger.error("Failed to load config.properties: {}", e.getMessage(), e);
        }
    }

    public String getProperty(String key, String defaultValue) {
        // System property takes precedence over config.properties
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        return properties.getProperty(key, defaultValue);
    }

    public String getBaseUrl() {
        return getProperty("baseUrl", "http://localhost:8080");
    }

    public String getApiBaseUrl() {
        return getProperty("apiBaseUrl", "http://localhost:8080/api");
    }

    public String getBrowser() {
        return getProperty("browser", "chrome");
    }

    public boolean isHeadless() {
        return Boolean.parseBoolean(getProperty("headless", "true"));
    }

    public int getExplicitWait() {
        return Integer.parseInt(getProperty("explicitWait", "10"));
    }

    public String getEmployeeUsername() {
        return getProperty("employee.username", "sathya.narayan");
    }

    public String getEmployeePassword() {
        return getProperty("employee.password", "Password@123");
    }

    public String getEmployeeId() {
        return getProperty("employee.id", "HCL10482");
    }

    public String getEmployeeName() {
        return getProperty("employee.name", "Sathya Narayan");
    }

    public String getManagerUsername() {
        return getProperty("manager.username", "rajesh.kumar");
    }

    public String getManagerPassword() {
        return getProperty("manager.password", "Manager@123");
    }

    public String getDatabaseType() {
        return getProperty("db.type", "file_json");
    }

    public String getDatabasePath() {
        return getProperty("db.path", "../employee-portal-app/backend/database/portal_data.json");
    }
}
